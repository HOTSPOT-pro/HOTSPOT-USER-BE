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
import hotspot.user.kafka.outbox.NotificationUserAlertOutboxPublisher;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.port.UpdatePolicySubService;
import hotspot.user.policy.controller.request.UpdatePolicySubRequest;
import hotspot.user.policy.controller.response.UpdatePolicySubResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.DateSnapshot;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.domain.mapper.BlockPolicyMapper;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.PolicySubRepository;
import lombok.RequiredArgsConstructor;

/**
 * 회선에 적용된 차단 정책을 갱신한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdatePolicySubServiceImpl implements UpdatePolicySubService {

    private final PolicySubRepository policySubRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final BlockPolicyRepository blockPolicyRepository;
    private final NotificationUserAlertOutboxPublisher userAlertOutboxPublisher;

    @Override
    public UpdatePolicySubResponse updatePolicySub(
            UpdatePolicySubRequest request,
            Long requesterFamilyId,
            FamilyRole requesterRole) {

        // 요청자의 권한과 가족 소속을 검증한다.
        validateAuthorityAndFamily(request.subId(), requesterFamilyId, requesterRole);

        List<Long> targetIdsList = request.blockPolicyIdList();

        // 요청된 정책을 조회하고 누락된 ID가 없는지 확인한다.
        List<BlockPolicy> targetPolicies = blockPolicyRepository.findAllById(targetIdsList);
        if (!targetIdsList.isEmpty() && targetPolicies.size() != targetIdsList.size()) {
            throw new ApplicationException(PolicyErrorCode.POLICY_NOT_FOUND);
        }

        // 현재 활성 정책 매핑을 policyId 기준으로 정리한다.
        List<PolicySub> activeSubs = policySubRepository.findBySubId(request.subId());
        Map<Long, PolicySub> activeMap = activeSubs.stream()
                .collect(Collectors.toMap(PolicySub::getPolicyId, sub -> sub));

        List<PolicySub> domainsToSave = new ArrayList<>();

        // 요청 정책은 기존 매핑을 soft-delete하고 스냅샷 기반 새 매핑을 추가한다.
        for (BlockPolicy policy : targetPolicies) {
            DateSnapshot newSnapshot = createSnapshotFrom(policy);

            if (activeMap.containsKey(policy.getId())) {
                PolicySub oldSub = activeMap.get(policy.getId());
                oldSub.delete();
                domainsToSave.add(oldSub);
                activeMap.remove(policy.getId());
            }

            PolicySub newSub = PolicySub.builder()
                    .subId(request.subId())
                    .policyId(policy.getId())
                    .dateSnapshot(newSnapshot)
                    .isDeleted(false)
                    .build();
            domainsToSave.add(newSub);
        }

        // 요청에서 제외된 기존 활성 매핑은 soft-delete 처리한다.
        for (PolicySub remainingSub : activeMap.values()) {
            remainingSub.delete();
            domainsToSave.add(remainingSub);
        }

        policySubRepository.saveAll(domainsToSave);
        publishPolicyAppliedAlerts(targetPolicies, request.subId(), requesterFamilyId);
        publishPolicyReleasedAlerts(activeMap.values(), request.subId(), requesterFamilyId);

        return BlockPolicyMapper.toUpdatePolicySubResponse(
                requesterFamilyId,
                request.subId(),
                targetIdsList
        );
    }

    // 요청자 권한과 가족 소유 관계를 검증한다.
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

    // 정책-회선 매핑에 저장할 스냅샷을 생성한다.
    private DateSnapshot createSnapshotFrom(BlockPolicy policy) {
        return DateSnapshot.builder()
                .policyName(policy.getName())
                .policyType(policy.getPolicyType())
                .data(policy.getPolicySnapshot())
                .build();
    }

    // 적용된 정책에 대한 알림 outbox 이벤트를 발행한다.
    private void publishPolicyAppliedAlerts(List<BlockPolicy> policies, Long subId, Long familyId) {
        for (BlockPolicy policy : policies) {
            userAlertOutboxPublisher.publishPolicyApplied(
                    subId,
                    familyId,
                    policy.getName(),
                    policy.getPolicyType()
            );
        }
    }

    // 해제된 정책에 대한 알림 outbox 이벤트를 발행한다.
    private void publishPolicyReleasedAlerts(Iterable<PolicySub> removedPolicies, Long subId, Long familyId) {
        for (PolicySub removedPolicy : removedPolicies) {
            DateSnapshot snapshot = removedPolicy.getDateSnapshot();
            String policyName = snapshot != null ? snapshot.getPolicyName() : "policy";
            PolicyType policyType = snapshot != null ? snapshot.getPolicyType() : null;
            userAlertOutboxPublisher.publishPolicyReleased(
                    subId,
                    familyId,
                    policyName,
                    policyType
            );
        }
    }
}
