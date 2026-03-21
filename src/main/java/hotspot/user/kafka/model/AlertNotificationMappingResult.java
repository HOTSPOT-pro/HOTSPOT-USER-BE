package hotspot.user.kafka.model;

import hotspot.user.kafka.domain.NotificationType;

public record AlertNotificationMappingResult(
        NotificationType notificationType,
        AlertNotificationContent content
) {
}
