package hotspot.user.notification.controller.response;

import java.util.List;

import lombok.Builder;

@Builder
public record NotificationAllowListResponse(
        List<NotificationAllowResponse> notificationAllows
) {
}
