package hotspot.user.policy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.common.exception.code.PolicyErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.outbox.notificationOutbox.domain.event.AlertAction;
import hotspot.user.outbox.notificationOutbox.domain.event.PolicyAlertOutboxEvent;
import hotspot.user.outbox.notificationOutbox.service.port.UserAlertNotificationOutboxPort;
import hotspot.user.policy.controller.port.UpdatePolicySubService;
import hotspot.user.policy.controller.request.UpdatePolicySubRequest;
import hotspot.user.policy.controller.response.UpdatePolicySubResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.domain.mapper.PolicySubMapper;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.PolicySubRepository;
import hotspot.user.policy.service.util.PolicyBlockSnapshotPublisher;
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
    private final UserAlertNotificationOutboxPort userAlertNotificationOutboxPort;
    private final PolicyBlockSnapshotPublisher policyBlockSnapshotPublisher;


    @Override
    public UpdatePolicySubResponse updatePolicySub(
            UpdatePolicySubRequest request,
            Long requesterMemberId,
            FamilyRole requesterRole) {

        // 1. 요청자의 권한을 먼저 검증한다. (OWNER만 가능)
        if (requesterRole != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        // 2. 요청자의 최신 가족 ID 조회 (memberId 기반)
        FamilySubscription requesterFamilySub = familySubscriptionRepository.findByMemberId(requesterMemberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        Long requesterFamilyId = requesterFamilySub.getFamily().getId();

        // 3. 타겟 회선의 가족 소속을 검증한다.
        validateTargetFamily(request.subId(), requesterFamilyId);

        List<Long> targetIdsList = request.blockPolicyIdList();

        // 4. 해당 회선의 모든 정책 매핑 정보를 먼저 조회한다. (활성 + 비활성 포함)
        List<PolicySub> existingSubs = policySubRepository.findBySubId(request.subId());

        // 5. 쿼리 최적화: 신규 요청 ID와 기존 등록된 ID를 모두 합쳐 단 1번의 DB 조회를 수행한다.
        Set<Long> allRequiredPolicyIds = existingSubs.stream()
                .map(PolicySub::getBlockPolicyId)
                .collect(Collectors.toSet());

        allRequiredPolicyIds.addAll(targetIdsList);

        Map<Long, BlockPolicy> policyMap = Map.of();
        if (!allRequiredPolicyIds.isEmpty()) {
            policyMap = blockPolicyRepository.findAllById(new ArrayList<>(allRequiredPolicyIds)).stream()
                    .collect(Collectors.toMap(BlockPolicy::getId, p -> p));
        }

        // 6. 요청된 타겟 정책 검증
        for (Long targetId : targetIdsList) {
            BlockPolicy policy = policyMap.get(targetId);
            if (policy == null) {
                throw new ApplicationException(PolicyErrorCode.POLICY_NOT_FOUND);
            }
            if (!policy.isAllowedTo(requesterFamilyId)) {
                throw new ApplicationException(PolicyErrorCode.POLICY_ACCESS_DENIED);
            }
            if (!policy.isActive()) {
                throw new ApplicationException(PolicyErrorCode.INACTIVE_POLICY_CANNOT_APPLY);
            }
        }

        Map<Long, PolicySub> existingMap = existingSubs.stream()
                .collect(Collectors.toMap(PolicySub::getBlockPolicyId, sub -> sub));

        List<PolicySub> domainsToSave = new ArrayList<>();
        List<BlockPolicy> appliedAlertPolicies = new ArrayList<>();
        List<BlockPolicy> releasedAlertPolicies = new ArrayList<>();

        // 7. 요청된 정책들을 순회하며 신규 추가 또는 활성화 처리
        for (Long targetId : targetIdsList) {
            BlockPolicy policy = policyMap.get(targetId);
            PolicySub sub = existingMap.remove(targetId);

            if (sub != null) {
                if (!sub.isActive()) {
                    sub.updateIsActive(true);
                    domainsToSave.add(sub);
                    appliedAlertPolicies.add(policy);
                }
            } else {
                PolicySub newSub = PolicySub.builder()
                        .subId(request.subId())
                        .blockPolicyId(targetId)
                        .isActive(true)
                        .build();
                domainsToSave.add(newSub);
                appliedAlertPolicies.add(policy);
            }
        }

        // 8. 요청에 없는데 DB에는 활성 상태로 남아있는 정책들을 비활성화 처리
        for (PolicySub remainingSub : existingMap.values()) {
            if (remainingSub.isActive()) {
                remainingSub.updateIsActive(false);
                domainsToSave.add(remainingSub);

                BlockPolicy policy = policyMap.get(remainingSub.getBlockPolicyId());
                if (policy != null) {
                    releasedAlertPolicies.add(policy);
                }
            }
        }

        // 9. 변경 사항이 있는 경우에만 저장 및 알림 발송
        if (!domainsToSave.isEmpty()) {
            policySubRepository.saveAll(domainsToSave);
            publishPolicyAppliedAlerts(appliedAlertPolicies, request.subId(), requesterFamilyId);
            publishPolicyReleasedAlerts(releasedAlertPolicies, request.subId(), requesterFamilyId);


            List<PolicySub> activeSubs =
                    policySubRepository.findActiveBySubId(request.subId());

            policyBlockSnapshotPublisher.publish(
                    request.subId(),
                    activeSubs
            );
        }

        return PolicySubMapper.toUpdatePolicySubResponse(
                requesterFamilyId,
                request.subId(),
                targetIdsList
        );
    }

    // 타겟 회선의 가족 소유 관계를 검증한다.
    private void validateTargetFamily(Long subId, Long requesterFamilyId) {
        FamilySubscription familySub = familySubscriptionRepository.findBySubId(subId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        if (!familySub.getFamily().getId().equals(requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }
    }

    private void publishPolicyAppliedAlerts(List<BlockPolicy> policies, Long subId, Long familyId) {
        for (BlockPolicy policy : policies) {
            userAlertNotificationOutboxPort.appendPolicyAlert(new PolicyAlertOutboxEvent(
                    subId,
                    familyId,
                    policy.getName(),
                    policy.getPolicyType(),
                    AlertAction.APPLIED
            ));
        }
    }

    private void publishPolicyReleasedAlerts(List<BlockPolicy> policies, Long subId, Long familyId) {
        for (BlockPolicy policy : policies) {
            userAlertNotificationOutboxPort.appendPolicyAlert(new PolicyAlertOutboxEvent(
                    subId,
                    familyId,
                    policy.getName(),
                    policy.getPolicyType(),
                    AlertAction.RELEASED
            ));
        }
    }
}
