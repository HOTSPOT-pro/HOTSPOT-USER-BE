package hotspot.user.policy.controller.request;

/**
 * 앱 서비스 차단 상태 dto
 * @param blockedServiceId
 * @param isBlocked
 */

public record AppBlockedServiceStatus(
        Long blockedServiceId,
        boolean isBlocked
) {
}
