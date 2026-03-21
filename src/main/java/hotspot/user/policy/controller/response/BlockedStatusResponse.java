package hotspot.user.policy.controller.response;

import java.util.List;

import lombok.Builder;

/**
 * 회선별 차단 상태 정보 응답 dto
 * @param isImmediateBlocked 즉시 차단 여부 (isLocked)
 * @param isCurrentlyBlocked 현재 정책 또는 즉시 차단에 의해 차단 중인지 여부
 * @param blockedPolicies 현재 차단 중인 정책 목록
 */
@Builder
public record BlockedStatusResponse(
        boolean isImmediateBlocked,
        boolean isCurrentlyBlocked,
        List<BlockedReasonResponse> blockedPolicies
) {

}
