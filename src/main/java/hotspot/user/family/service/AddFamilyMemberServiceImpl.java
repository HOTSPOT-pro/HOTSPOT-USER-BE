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
import hotspot.user.common.crpyto.SubscriptionKeyInfo;
import hotspot.user.common.crpyto.SubscriptionKeyLookup;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.common.util.s3.S3Util;
import hotspot.user.family.controller.port.AddFamilyMemberService;
import hotspot.user.family.controller.request.AddFamilyMemberRequest;
import hotspot.user.family.controller.request.FamilyMemberRequest;
import hotspot.user.family.controller.response.AddFamilyMemberResponse;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyApplyTarget;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.mapper.FamilyApplyMapper;
import hotspot.user.family.service.port.FamilyApplyRepository;
import hotspot.user.family.service.port.FamilyApplyTargetRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

/**
 * 가족 구성원 신청 서비스 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional
public class AddFamilyMemberServiceImpl implements AddFamilyMemberService {

    private final SubscriptionRepository subscriptionRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final FamilyApplyRepository familyApplyRepository;
    private final FamilyApplyTargetRepository familyApplyTargetRepository;
    private final PhoneHashIndexer phoneHashIndexer;
    private final SubscriptionKeyLookup subscriptionKeyLookup;
    private final PhoneDecryptor phoneDecryptor;
    private final S3Util s3Util;

    @Override
    public AddFamilyMemberResponse addFamilyMember(
            Long requesterMemberId,
            Long familyId,
            AddFamilyMemberRequest request) {

        // 1. 요청자의 권한 및 가족 소속 여부 검증
        FamilySubscription requesterFs = familySubscriptionRepository.findByMemberId(requesterMemberId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        if (!requesterFs.getFamily().getId().equals(familyId) || requesterFs.getFamilyRole() != FamilyRole.OWNER) {
            throw new ApplicationException(FamilyErrorCode.ONLY_OWNER_CAN_MANAGE);
        }

        // 2. 추가 신청 타입 검증 (ADD만 허용)
        if (request.applyType() != ApplyType.ADD) {
            throw new ApplicationException(FamilyErrorCode.INVALID_APPLY_TYPE);
        }

        Subscription requesterSub = requesterFs.getSubscription();

        // 3. 피신청자들의 정보 매핑 (Hash 기반 매핑)
        Map<String, FamilyMemberRequest> hashToRequestMap = request.familyMemberList().stream()
                .collect(Collectors.toMap(r -> phoneHashIndexer.toHash(r.phone()), r -> r));

        List<String> phoneHashes = new ArrayList<>(hashToRequestMap.keySet());

        // 4. Subscription 일괄 조회
        List<Subscription> subscriptions = subscriptionRepository.findAllByPhoneHashIn(phoneHashes);
        if (subscriptions.size() != phoneHashes.size()) {
            throw new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND);
        }

        List<Long> subIds = subscriptions.stream()
                .map(Subscription::getId)
                .toList();

        // 5. 가족 소속 및 중복 신청 여부 일괄 조회
        Set<Long> existingFamilySubIds = familySubscriptionRepository.findAllBySubIdIn(subIds).stream()
                .map(fs -> fs.getSubscription().getId())
                .collect(Collectors.toSet());

        Set<Long> pendingApplySubIds = familyApplyTargetRepository.findAllPendingByTargetSubIdIn(subIds).stream()
                .map(FamilyApplyTarget::getTargetSubId)
                .collect(Collectors.toSet());

        // 6. 검증 및 타겟 도메인 생성
        List<FamilyApplyTarget> targets = new ArrayList<>();
        for (Subscription sub : subscriptions) {
            // 본인 추가 방지
            if (sub.getId().equals(requesterSub.getId())) {
                throw new ApplicationException(FamilyErrorCode.TARGET_ALREADY_IN_FAMILY);
            }

            validateMember(sub.getId(), existingFamilySubIds, pendingApplySubIds);

            // Hash로 요청 정보 매칭
            FamilyMemberRequest memberRequest = hashToRequestMap.get(sub.getPhoneHash());

            // 타인에게 OWNER 부여 금지 검증
            if (memberRequest.targetFamilyRole() == FamilyRole.OWNER) {
                throw new ApplicationException(FamilyErrorCode.CANNOT_ASSIGN_OWNER_ROLE);
            }

            targets.add(FamilyApplyMapper.toFamilyApplyTarget(null, sub.getId(), memberRequest.targetFamilyRole()));
        }

        // 7. 신청 저장
        // 전달 받은 S3 임시 버킷 -> 메인 버킷으로 변경
        String certificatedKey = s3Util.moveTempToCertificate(request.s3TempKey());

        FamilyApply familyApply = FamilyApplyMapper
                .toFamilyApply(requesterSub.getId(), familyId, request, certificatedKey);
        FamilyApply savedApply = familyApplyRepository.save(familyApply);

        // 8. 신청 타겟들 저장
        List<FamilyApplyTarget> finalTargets = targets.stream()
                .map(t -> FamilyApplyMapper.toFamilyApplyTarget(
                        savedApply.getId(),
                        t.getTargetSubId(),
                        t.getTargetFamilyRole()))
                .toList();

        List<FamilyApplyTarget> savedTargets = familyApplyTargetRepository.saveAll(finalTargets);

        // 9. 응답 변환
        Map<Long, Subscription> subscriptionMap = subscriptions.stream()
                .collect(Collectors.toMap(Subscription::getId, s -> s));

        Map<Long, SubscriptionKeyInfo> keyInfoBySubId = subscriptionKeyLookup.findKeyInfosBySubIds(
                subscriptions.stream().map(Subscription::getId).toList()
        );

        Map<Long, String> subIdToPhoneMap = subscriptions.stream()
                .collect(Collectors.toMap(
                        Subscription::getId,
                        s -> phoneDecryptor.decrypt(s.getPhoneEnc(), keyInfoBySubId.get(s.getId()))
                ));

        return FamilyApplyMapper.toAddFamilyMemberResponse(
                familyId,
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
