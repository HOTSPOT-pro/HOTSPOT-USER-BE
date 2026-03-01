package hotspot.user.policy.controller.request;

import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicyType;

/**
 * 정책 생성 / 수정 request dto
 */
public record BlockPolicyRequest(
        Long id,
        String name,
        Long familyId,
        PolicyType policyType,
        PolicySnapshot policySnapshot,
        String policyDescription,
        boolean isActive
) {
}
