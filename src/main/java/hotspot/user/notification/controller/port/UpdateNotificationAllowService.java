package hotspot.user.notification.controller.port;

import hotspot.user.notification.controller.request.UpdateNotificationAllowRequest;
import hotspot.user.notification.controller.response.NotificationAllowResponse;

public interface UpdateNotificationAllowService {
    NotificationAllowResponse updateNotificationAllow(Long memberId, UpdateNotificationAllowRequest request);
}
