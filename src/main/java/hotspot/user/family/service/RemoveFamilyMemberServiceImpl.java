package hotspot.user.family.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.RemoveFamilyMemberService;
import hotspot.user.family.controller.request.RemoveFamilyMemberRequest;
import hotspot.user.family.controller.response.RemoveFamilyMemberResponse;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyApplyTarget;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.mapper.FamilyApplyMapper;
import hotspot.user.family.domain.mapper.RemoveFamilyMemberMapper;
import hotspot.user.family.service.port.FamilyApplyRepository;
import hotspot.user.family.service.port.FamilyApplyTargetRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import lombok.RequiredArgsConstructor;

/**
 * 가족 구성원 삭제 신청 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RemoveFamilyMemberServiceImpl implements RemoveFamilyMemberService {

    private final FamilyApplyRepository familyApplyRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final FamilyApplyTargetRepository familyApplyTargetRepository;

    @Override
    public RemoveFamilyMemberResponse removeFamilyMember(Long requesterMemberId, RemoveFamilyMemberRequest request) {

        // 1. 요청자 권한 및 소속 확인 (방장 여부)
        FamilySubscription requesterFs = familySubscriptionRepository.findByMemberId(requesterMemberId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        if (requesterFs.getFamilyRole() != FamilyRole.OWNER) {
            throw new ApplicationException(FamilyErrorCode.ONLY_OWNER_CAN_MANAGE);
        }

        Long familyId = requesterFs.getFamily().getId();

        // 2. 요청 리스트 중복 제거 및 데이터 준비
        List<Long> targetSubIds = request.targetSubIdList().stream()
                .distinct()
                .toList();

        // 3. 삭제 대상자들의 가족 소속 확인 및 중복 신청 여부 확인 (N+1 방지)
        List<FamilySubscription> targetFsList = familySubscriptionRepository.findAllBySubIdIn(targetSubIds);

        // 회선 존재 여부 검증
        if (targetFsList.size() != targetSubIds.size()) {
            throw new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND);
        }

        // 모든 대상이 현재 방장과 같은 가족 소속인지 확인 (선언적 검증)
        boolean allInSameFamily = targetFsList.stream()
                .allMatch(fs -> fs.getFamily().getId().equals(familyId));

        if (!allInSameFamily) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }

        // 이미 처리 대기 중인(PENDING) 신청이 있는지 확인 (FamilyApplyTarget 테이블 조회)
        List<FamilyApplyTarget> existingApplies = familyApplyTargetRepository
                .findAllPendingByTargetSubIdIn(targetSubIds);

        if (!existingApplies.isEmpty()) {
            throw new ApplicationException(FamilyErrorCode.DUPLICATE_FAMILY_APPLY);
        }

        // 4. 신청서 저장 (부모)
        FamilyApply apply = FamilyApplyMapper.toFamilyApply(requesterFs.getSubscription().getId(), familyId, request);
        FamilyApply savedApply = familyApplyRepository.save(apply);

        // 5. 삭제 대상자 리스트 생성 및 일괄 저장 (자식)
        // 이미 조회된 targetFsList를 활용해 각 사용자의 현재 역할을 주입
        Map<Long, FamilyRole> subIdToRoleMap = targetFsList.stream()
                .collect(Collectors.toMap(fs -> fs.getSubscription().getId(), FamilySubscription::getFamilyRole));

        List<FamilyApplyTarget> targets = targetSubIds.stream()
                .map(subId -> FamilyApplyMapper.toFamilyApplyTarget(
                        savedApply.getId(),
                        subId,
                        subIdToRoleMap.get(subId)))
                .toList();

        List<FamilyApplyTarget> savedTargets = familyApplyTargetRepository.saveAll(targets);

        // 6. 응답 반환
        return RemoveFamilyMemberMapper.toRemoveFamilyMemberResponse(savedApply, savedTargets);
    }
}
