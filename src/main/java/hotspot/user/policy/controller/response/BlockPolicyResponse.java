package hotspot.user.policy.controller.response;

import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicyType;
import lombok.Builder;

/**
 * 관리자 정책 응답 dto
 */
@Builder
public record BlockPolicyResponse(
        Long id,
        String name,
        Long familyId,
        PolicyType policyType,
        PolicySnapshot policySnapshot,
        String policyDescription,
        boolean isActive
) {
}
