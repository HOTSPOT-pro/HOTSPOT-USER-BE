package hotspot.user.outbox.consistencyOutbox.infrastructure;

import org.springframework.stereotype.Repository;

import hotspot.user.outbox.consistencyOutbox.domain.OutboxEvent;
import hotspot.user.outbox.consistencyOutbox.infrastructure.entity.OutboxEventEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepository {

    private final OutboxEventJpaRepository jpaRepository;

    public void save(OutboxEvent outboxEvent) {
        jpaRepository.save(OutboxEventEntity.domainToEntity(outboxEvent));
    }
}
