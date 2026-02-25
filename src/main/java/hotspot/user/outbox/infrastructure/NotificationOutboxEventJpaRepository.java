package hotspot.user.outbox.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.outbox.infrastructure.entity.NotificationOutboxEventEntity;

public interface NotificationOutboxEventJpaRepository extends JpaRepository<NotificationOutboxEventEntity, UUID> {
}
