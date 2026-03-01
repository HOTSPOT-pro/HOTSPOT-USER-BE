package hotspot.user.outbox.notificationOutbox.infrastructure;

import org.springframework.stereotype.Repository;

import hotspot.user.outbox.notificationOutbox.domain.NotificationOutboxEvent;
import hotspot.user.outbox.notificationOutbox.infrastructure.entity.NotificationOutboxEventEntity;
import hotspot.user.outbox.notificationOutbox.service.port.NotificationOutboxEventRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationOutboxEventRepositoryImpl implements NotificationOutboxEventRepository {

    private final NotificationOutboxEventJpaRepository notificationOutboxEventJpaRepository;

    @Override
    public NotificationOutboxEvent save(NotificationOutboxEvent event) {
        NotificationOutboxEventEntity saved = notificationOutboxEventJpaRepository.save(
                NotificationOutboxEventEntity.builder()
                        .id(event.id())
                        .aggregateType(event.aggregateType())
                        .aggregateId(event.aggregateId())
                        .type(event.type())
                        .payload(event.payload())
                        .build()
        );
        return new NotificationOutboxEvent(
                saved.getId(),
                saved.getAggregateType(),
                saved.getAggregateId(),
                saved.getType(),
                saved.getPayload()
        );
    }
}
