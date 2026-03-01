package hotspot.user.outbox.notificationOutbox.service.port;

import hotspot.user.outbox.notificationOutbox.domain.NotificationOutboxEvent;

public interface NotificationOutboxEventRepository {

    NotificationOutboxEvent save(NotificationOutboxEvent event);
}
