package hotspot.user.outbox.notificationOutbox.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.outbox.notificationOutbox.domain.NotificationOutboxEvent;
import hotspot.user.outbox.notificationOutbox.infrastructure.entity.NotificationOutboxEventEntity;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxEventRepositoryImplTest {

    @Mock
    private NotificationOutboxEventJpaRepository notificationOutboxEventJpaRepository;

    @InjectMocks
    private NotificationOutboxEventRepositoryImpl notificationOutboxEventRepository;

    @Test
    @DisplayName("save maps domain to entity and returns mapped domain")
    void saveSuccess() {
        UUID id = UUID.randomUUID();
        NotificationOutboxEvent event = new NotificationOutboxEvent(
                id,
                "user-alert",
                "101",
                "POLICY_APPLIED",
                "{\"key\":\"value\"}"
        );

        NotificationOutboxEventEntity savedEntity = NotificationOutboxEventEntity.builder()
                .id(id)
                .aggregateType("user-alert")
                .aggregateId("101")
                .type("POLICY_APPLIED")
                .payload("{\"key\":\"value\"}")
                .build();
        given(notificationOutboxEventJpaRepository.save(any(NotificationOutboxEventEntity.class)))
                .willReturn(savedEntity);

        NotificationOutboxEvent saved = notificationOutboxEventRepository.save(event);

        ArgumentCaptor<NotificationOutboxEventEntity> captor =
                ArgumentCaptor.forClass(NotificationOutboxEventEntity.class);
        then(notificationOutboxEventJpaRepository).should().save(captor.capture());

        NotificationOutboxEventEntity requestedEntity = captor.getValue();
        assertThat(requestedEntity.getId()).isEqualTo(id);
        assertThat(requestedEntity.getAggregateType()).isEqualTo("user-alert");
        assertThat(requestedEntity.getAggregateId()).isEqualTo("101");
        assertThat(requestedEntity.getType()).isEqualTo("POLICY_APPLIED");
        assertThat(requestedEntity.getPayload()).isEqualTo("{\"key\":\"value\"}");

        assertThat(saved.id()).isEqualTo(id);
        assertThat(saved.aggregateType()).isEqualTo("user-alert");
        assertThat(saved.aggregateId()).isEqualTo("101");
        assertThat(saved.type()).isEqualTo("POLICY_APPLIED");
        assertThat(saved.payload()).isEqualTo("{\"key\":\"value\"}");
    }
}
