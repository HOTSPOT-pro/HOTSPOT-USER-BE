package hotspot.user.notification.controller.response;

import hotspot.user.notification.domain.NotificationCategory;
import lombok.Builder;

@Builder
public record NotificationAllowResponse(
        NotificationCategory notificationCategory,
        Boolean notificationAllow
) {
}
