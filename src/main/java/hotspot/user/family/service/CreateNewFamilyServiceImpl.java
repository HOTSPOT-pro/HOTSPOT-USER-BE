package hotspot.user.family.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.common.crpyto.PhoneHashIndexer;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.common.util.s3.S3Util;
import hotspot.user.family.controller.port.CreateNewFamilyService;
import hotspot.user.family.controller.request.CreateNewFamilyRequest;
import hotspot.user.family.controller.request.FamilyMemberRequest;
import hotspot.user.family.controller.response.CreateNewFamilyResponse;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyApplyTarget;
import hotspot.user.family.domain.mapper.FamilyApplyMapper;
import hotspot.user.family.service.port.FamilyApplyRepository;
import hotspot.user.family.service.port.FamilyApplyTargetRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

/**
 * 신규 가족 생성 서비스 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional
public class CreateNewFamilyServiceImpl implements CreateNewFamilyService {

    private final SubscriptionRepository subscriptionRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final FamilyApplyRepository familyApplyRepository;
    private final FamilyApplyTargetRepository familyApplyTargetRepository;
    private final PhoneHashIndexer phoneHashIndexer;
    private final PhoneDecryptor phoneDecryptor;
    private final S3Util s3Util;

    @Override
    public CreateNewFamilyResponse createNewFamily(
            Long requesterMemberId,
            CreateNewFamilyRequest request) {

        // 1. 이미 가족에 소속되어 있는지 확인 (신규 생성의 전제 조건)
        if (familySubscriptionRepository.findByMemberId(requesterMemberId).isPresent()) {
            throw new ApplicationException(FamilyErrorCode.TARGET_ALREADY_IN_FAMILY);
        }

        // 2. 신청 타입 검증
        if (request.applyType() != ApplyType.CREATE) {
            throw new ApplicationException(FamilyErrorCode.INVALID_APPLY_TYPE);
        }

        // 3. 신청자(본인) 회선 정보 조회
        Subscription requesterSub = subscriptionRepository.findByMemberId(requesterMemberId)
                .orElseThrow(() -> new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 4. 피신청자 정보 조회 및 배치 처리를 위한 데이터 준비
        Map<String, FamilyMemberRequest> hashToRequestMap = request.familyMemberList().stream()
                .collect(Collectors.toMap(r -> phoneHashIndexer.toHash(r.phone()), r -> r));

        List<String> phoneHashes = new ArrayList<>(hashToRequestMap.keySet());
        List<Subscription> targetSubscriptions = subscriptionRepository.findAllByPhoneHashIn(phoneHashes);

        if (targetSubscriptions.size() != phoneHashes.size()) {
            throw new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND);
        }

        List<Long> targetSubIds = targetSubscriptions.stream().map(Subscription::getId).toList();

        // 5. 중복 소속 및 중복 신청 여부 일괄 조회
        Set<Long> existingFamilySubIds = familySubscriptionRepository.findAllBySubIdIn(targetSubIds).stream()
                .map(fs -> fs.getSubscription().getId())
                .collect(Collectors.toSet());

        Set<Long> pendingApplySubIds = familyApplyTargetRepository.findAllPendingByTargetSubIdIn(targetSubIds).stream()
                .map(FamilyApplyTarget::getTargetSubId)
                .collect(Collectors.toSet());

        // 6. 타겟 도메인 리스트 구성
        List<FamilyApplyTarget> targets = new ArrayList<>();

        // 6-1. 신청자 본인을 OWNER로 추가
        targets.add(FamilyApplyMapper.toFamilyApplyTarget(null, requesterSub.getId(), FamilyRole.OWNER));

        // 6-2. 피신청자들 검증 및 추가
        for (Subscription sub : targetSubscriptions) {
            // 본인 중복 방지
            if (sub.getId().equals(requesterSub.getId())) {
                continue;
            }

            validateMember(sub.getId(), existingFamilySubIds, pendingApplySubIds);

            FamilyMemberRequest memberRequest = hashToRequestMap.get(sub.getPhoneHash());
            if (memberRequest.targetFamilyRole() == FamilyRole.OWNER) {
                throw new ApplicationException(FamilyErrorCode.CANNOT_ASSIGN_OWNER_ROLE);
            }

            targets.add(FamilyApplyMapper.toFamilyApplyTarget(null, sub.getId(), memberRequest.targetFamilyRole()));
        }

        // 7. 신청 정보 저장 (familyId는 신규 생성이므로 null)
        // 전달 받은 S3 임시 버킷 -> 메인 버킷으로 변경
        String certificatedKey = s3Util.moveTempToCertificate(request.s3TempKey());

        FamilyApply familyApply = FamilyApplyMapper.toFamilyApply(requesterSub.getId(), null, request, certificatedKey);
        FamilyApply savedApply = familyApplyRepository.save(familyApply);

        // 8. 타겟들 ID 연결 및 일괄 저장
        List<FamilyApplyTarget> finalTargets = targets.stream()
                .map(t -> FamilyApplyMapper.toFamilyApplyTarget(
                        savedApply.getId(),
                        t.getTargetSubId(),
                        t.getTargetFamilyRole()))
                .toList();

        List<FamilyApplyTarget> savedTargets = familyApplyTargetRepository.saveAll(finalTargets);

        // 9. 응답 변환 (복호화 데이터 준비 포함)
        List<Subscription> allInvolvedSubs = new ArrayList<>();
        allInvolvedSubs.add(requesterSub); // 본인 포함
        allInvolvedSubs.addAll(targetSubscriptions);

        Map<Long, Subscription> subscriptionMap = allInvolvedSubs.stream()
                .collect(Collectors.toMap(
                        Subscription::getId,
                        s -> s,
                        (existing, replacement) -> existing));

        Map<Long, String> subIdToPhoneMap = allInvolvedSubs.stream()
                .collect(Collectors.toMap(
                        Subscription::getId,
                        s -> phoneDecryptor.decrypt(s.getPhoneEnc()),
                        (existing, replacement) -> existing));

        return FamilyApplyMapper.toCreateNewFamilyResponse(
                null, // familyId는 아직 없음
                request.applyType(),
                savedTargets,
                subscriptionMap,
                subIdToPhoneMap
        );
    }

    private void validateMember(Long targetSubId, Set<Long> existingFamilySubIds, Set<Long> pendingApplySubIds) {
        if (existingFamilySubIds.contains(targetSubId)) {
            throw new ApplicationException(FamilyErrorCode.TARGET_ALREADY_IN_FAMILY);
        }

        if (pendingApplySubIds.contains(targetSubId)) {
            throw new ApplicationException(FamilyErrorCode.DUPLICATE_FAMILY_APPLY);
        }
    }
}
