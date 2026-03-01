package hotspot.user.outbox.notificationOutbox.domain;

import java.util.UUID;

public record NotificationOutboxEvent(
        UUID id,
        String aggregateType,
        String aggregateId,
        String type,
        String payload
) {
}
