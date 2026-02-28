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
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.domain.mapper.PolicySubMapper;
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

        // 정책 접근 권한 검증: 관리자 정책 또는 우리 가족 정책인지 확인
        for (BlockPolicy policy : targetPolicies) {
            if (!policy.isAllowedTo(requesterFamilyId)) {
                throw new ApplicationException(PolicyErrorCode.POLICY_ACCESS_DENIED);
            }

            // 정책 상태 검증: 활성 상태인 정책만 적용 가능
            if (!policy.isActive()) {
                throw new ApplicationException(PolicyErrorCode.INACTIVE_POLICY_CANNOT_APPLY);
            }
        }

        // 해당 회선의 모든 정책 매핑 정보를 조회한다. (활성 + 비활성 포함)
        List<PolicySub> existingSubs = policySubRepository.findBySubId(request.subId());
        Map<Long, PolicySub> existingMap = existingSubs.stream()
                .collect(Collectors.toMap(PolicySub::getBlockPolicyId, sub -> sub));

        List<PolicySub> domainsToSave = new ArrayList<>();
        List<BlockPolicy> appliedAlertPolicies = new ArrayList<>();
        List<PolicySub> releasedAlertSubs = new ArrayList<>();

        // 1. 요청된 정책들을 순회하며 신규 추가 또는 활성화 처리
        for (BlockPolicy policy : targetPolicies) {
            if (existingMap.containsKey(policy.getId())) {
                PolicySub sub = existingMap.get(policy.getId());
                // 비활성 상태인 경우에만 활성화 및 알림 대상 추가
                if (!sub.isActive()) {
                    sub.updateIsActive(true);
                    domainsToSave.add(sub);
                    appliedAlertPolicies.add(policy);
                }
                // 처리 완료된 항목은 Map에서 제거 (나머지는 삭제 대상)
                existingMap.remove(policy.getId());
            } else {
                // DB에 아예 없는 경우 신규 생성 및 알림 대상 추가
                PolicySub newSub = PolicySub.builder()
                        .subId(request.subId())
                        .blockPolicyId(policy.getId())
                        .isActive(true)
                        .build();
                domainsToSave.add(newSub);
                appliedAlertPolicies.add(policy);
            }
        }

        // 2. 요청에 없는데 DB에는 활성 상태로 남아있는 정책들을 비활성화 처리
        for (PolicySub remainingSub : existingMap.values()) {
            if (remainingSub.isActive()) {
                remainingSub.updateIsActive(false);
                domainsToSave.add(remainingSub);
                releasedAlertSubs.add(remainingSub);
            }
        }

        // 변경 사항이 있는 경우에만 저장 및 알림 발송
        if (!domainsToSave.isEmpty()) {
            policySubRepository.saveAll(domainsToSave);
            publishPolicyAppliedAlerts(appliedAlertPolicies, request.subId(), requesterFamilyId);
            publishPolicyReleasedAlerts(releasedAlertSubs, request.subId(), requesterFamilyId);
        }

        return PolicySubMapper.toUpdatePolicySubResponse(
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
    private void publishPolicyReleasedAlerts(List<PolicySub> removedPolicies, Long subId, Long familyId) {
        if (removedPolicies.isEmpty()) {
            return;
        }

        List<Long> policyIds = removedPolicies.stream().map(PolicySub::getBlockPolicyId).toList();
        Map<Long, BlockPolicy> policyMap = blockPolicyRepository.findAllById(policyIds).stream()
                .collect(Collectors.toMap(BlockPolicy::getId, p -> p));

        for (PolicySub removedPolicy : removedPolicies) {
            BlockPolicy policy = policyMap.get(removedPolicy.getBlockPolicyId());
            if (policy != null) {
                userAlertOutboxPublisher.publishPolicyReleased(
                        subId,
                        familyId,
                        policy.getName(),
                        policy.getPolicyType()
                );
            }
        }
    }
}
