package hotspot.user.notification.controller.port;

import hotspot.user.notification.controller.response.NotificationAllowListResponse;

public interface FindNotificationAllowService {
    NotificationAllowListResponse findNotificationAllows(Long memberId);
}
