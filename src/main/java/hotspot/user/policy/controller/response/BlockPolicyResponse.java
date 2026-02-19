package hotspot.user.policy.controller.response;

import hotspot.user.policy.domain.BlockPolicy;
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
        PolicyType policyType,
        PolicySnapshot policySnapshot
) {

    public static BlockPolicyResponse from(BlockPolicy blockPolicy) {
        return BlockPolicyResponse.builder()
                .id(blockPolicy.getId())
                .name(blockPolicy.getName())
                .policyType(blockPolicy.getPolicyType())
                .policySnapshot(blockPolicy.getPolicySnapshot())
                .build();
    }
}
