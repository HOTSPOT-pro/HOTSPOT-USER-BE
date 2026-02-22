package hotspot.user.policy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.PolicyErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.port.UpdateBlockPolicyService;
import hotspot.user.policy.controller.request.UpdateBlockPolicyRequest;
import hotspot.user.policy.controller.response.UpdateBlockPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.DateSnapshot;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.domain.mapper.BlockPolicyMapper;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.PolicySubRepository;
import hotspot.user.subscription.domain.Subscription;
import lombok.RequiredArgsConstructor;

/**
 * 구성원별 정책 업데이트 서비스 코드 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateBlockPolicyServiceImpl implements UpdateBlockPolicyService {

    private final PolicySubRepository policySubRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final BlockPolicyRepository blockPolicyRepository;

    @Override
    public UpdateBlockPolicyResponse updateBlockPolicy(
            UpdateBlockPolicyRequest request,
            Long requesterFamilyId,
            FamilyRole requesterRole) {

        // 1. 권한 및 가족 매핑 검증
        validateAuthorityAndFamily(request.subId(), requesterFamilyId, requesterRole);

        List<Long> targetIdsList = request.blockPolicyIdList();

        // 2. 요청된 원본 정책 전체 조회 & 예외 처리 로직
        List<BlockPolicy> targetPolicies = blockPolicyRepository.findAllById(targetIdsList);
        if (!targetIdsList.isEmpty() && targetPolicies.size() != targetIdsList.size()) {
            throw new ApplicationException(PolicyErrorCode.POLICY_NOT_FOUND);
        }

        // 3. 기존 DB 상태 조회 (현재 '활성화된(isDeleted=false)' 데이터만 조회)
        List<PolicySub> activeSubs = policySubRepository.findBySubId(request.subId());
        Map<Long, PolicySub> activeMap = activeSubs.stream()
                .collect(Collectors.toMap(PolicySub::getPolicyId, sub -> sub));

        // 영속화(Save/Update) 대상 도메인 객체들을 담을 리스트
        List<PolicySub> domainsToSave = new ArrayList<>();

        // 4. 요청된 타겟 정책들 처리 (기존 비활성화 + 신규 Insert)
        for (BlockPolicy policy : targetPolicies) {
            DateSnapshot newSnapshot = createSnapshotFrom(policy);

            // 해당 정책이 이미 활성화 상태라면, 기존 레코드를 비활성화(Delete) 처리
            if (activeMap.containsKey(policy.getId())) {
                PolicySub oldSub = activeMap.get(policy.getId());
                oldSub.delete(); // isDeleted = true 로 상태 변경
                domainsToSave.add(oldSub);

                activeMap.remove(policy.getId()); // 처리 완료된 항목은 Map에서 제거
            }

            Subscription subscription = Subscription.builder()
                    .id(request.subId())
                    .build();

            // 무조건 신규 도메인 객체 생성 (새로운 스냅샷으로 Insert)
            PolicySub newSub = PolicySub.builder()
                    .subId(request.subId())
                    .policyId(policy.getId())
                    .dateSnapshot(newSnapshot)
                    .isDeleted(false) // 활성화 상태로 생성
                    .build();
            domainsToSave.add(newSub);
        }

        // 5. 요청 목록에 없는 나머지 기존 활성 정책들 처리 (순수 비활성화)
        for (PolicySub remainingSub : activeMap.values()) {
            remainingSub.delete();
            domainsToSave.add(remainingSub);
        }

        // 6. 도메인 객체 리스트를 Port(어댑터)로 전달하여 일괄 영속화
        policySubRepository.saveAll(domainsToSave);

        // 7. 최종 결과 반환
        return BlockPolicyMapper.toUpdateBlockPolicyResponse(
                requesterFamilyId,
                request.subId(),
                targetIdsList
        );
    }

    // 권한 및 같은 가족인지 검증
    private void validateAuthorityAndFamily(Long subId, Long requesterFamilyId, FamilyRole requesterRole) {
        if (requesterRole != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        FamilySubscription familySub = familySubscriptionRepository.findBySubId(subId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        if (!familySub.getFamily().getId().equals(requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }
    }

    // PolicySub에 저장할 DateSnapshot 조립하는 메서드
    private DateSnapshot createSnapshotFrom(BlockPolicy policy) {
        return DateSnapshot.builder()
                .policyName(policy.getName())
                .policyType(policy.getPolicyType())
                .data(policy.getPolicySnapshot())
                .build();
    }
}
