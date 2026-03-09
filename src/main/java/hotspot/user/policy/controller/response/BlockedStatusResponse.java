package hotspot.user.policy.controller.response;

import lombok.Builder;

import java.util.List;

/**
 * 회선별 차단 상태 정보 응답 dto
 * @param subId
 * @param isImmediateBlocked
 * @param isCurrentlyBlocked
 * @param blockedPolicies
 */
@Builder
public record BlockedStatusResponse(
        Long subId,
        boolean isImmediateBlocked,
        boolean isCurrentlyBlocked,
        List<BlockedPolicyInfo> blockedPolicies
) {

    /**
     * 차단된 정책 정보 (id, 이름)
     * @param id
     * @param name
     */
    @Builder
    public record BlockedPolicyInfo(
            Long id,
            String name
    ) { }

}
