package hotspot.user.notification.controller.response;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String eventId,
        String notificationType,
        String content,
        Boolean isRead,
        LocalDateTime createdTime
) {
}
