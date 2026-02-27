package hotspot.user.outbox.consistencyOutbox.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.outbox.consistencyOutbox.domain.OutboxEvent;
import hotspot.user.outbox.consistencyOutbox.infrastructure.entity.OutboxEventEntity;

@ExtendWith(MockitoExtension.class)
class OutboxEventRepositoryTest {

    @Mock
    private OutboxEventJpaRepository jpaRepository;

    @InjectMocks
    private OutboxEventRepository outboxEventRepository;

    @Test
    @DisplayName("성공: 도메인 객체를 엔티티로 변환 후 JPA save를 호출한다")
    void saveSuccess() {
        // given
        OutboxEvent outboxEvent = OutboxEvent.create(
                "USER",
                "1",
                "USER_CREATED",
                "{\"key\":\"value\"}"
        );

        // when
        outboxEventRepository.save(outboxEvent);

        // then
        verify(jpaRepository).save(any(OutboxEventEntity.class));
    }
}
