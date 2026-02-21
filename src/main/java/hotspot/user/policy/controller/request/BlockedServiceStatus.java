package hotspot.user.policy.controller.request;

import java.util.Set;

/**
 * 앱 서비스 차단 상태 dto
 * @param blockedServiceId
 * @param isBlocked
 */

public record BlockedServiceStatus(
        Long blockedServiceId,
        boolean isBlocked
) {
}
