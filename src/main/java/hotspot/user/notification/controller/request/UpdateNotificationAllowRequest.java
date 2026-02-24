package hotspot.user.notification.controller.request;

import jakarta.validation.constraints.NotNull;

import hotspot.user.notification.domain.NotificationCategory;

public record UpdateNotificationAllowRequest(
        @NotNull NotificationCategory notificationCategory,
        @NotNull Boolean notificationAllow
) {
}
