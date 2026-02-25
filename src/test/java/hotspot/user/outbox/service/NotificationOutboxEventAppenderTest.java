package hotspot.user.outbox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.GlobalErrorCode;
import hotspot.user.outbox.infrastructure.NotificationOutboxEventJpaRepository;
import hotspot.user.outbox.infrastructure.entity.NotificationOutboxEventEntity;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxEventAppenderTest {

    @Mock
    private NotificationOutboxEventJpaRepository outboxEventJpaRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationOutboxEventAppender appender;

    @Test
    @DisplayName("append: payload를 JSON으로 저장한다")
    void appendSavesSerializedPayload() throws Exception {
        Map<String, Object> payload = Map.of("key", "value");
        given(objectMapper.writeValueAsString(payload)).willReturn("{\"key\":\"value\"}");

        appender.append("user-alert", "101", "POLICY_APPLIED", payload);

        ArgumentCaptor<NotificationOutboxEventEntity> captor =
                ArgumentCaptor.forClass(NotificationOutboxEventEntity.class);
        then(outboxEventJpaRepository).should().save(captor.capture());

        NotificationOutboxEventEntity entity = captor.getValue();
        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getAggregateType()).isEqualTo("user-alert");
        assertThat(entity.getAggregateId()).isEqualTo("101");
        assertThat(entity.getType()).isEqualTo("POLICY_APPLIED");
        assertThat(entity.getPayload()).isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    @DisplayName("append: 직렬화 실패 시 INTERNAL_SERVER_ERROR를 던진다")
    void appendThrowsWhenSerializationFails() throws Exception {
        Object payload = new Object();
        given(objectMapper.writeValueAsString(payload)).willThrow(new JsonProcessingException("boom") {
        });

        assertThatThrownBy(() -> appender.append("user-alert", "101", "POLICY_APPLIED", payload))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getCode())
                        .isEqualTo(GlobalErrorCode.INTERNAL_SERVER_ERROR));

        then(outboxEventJpaRepository).shouldHaveNoInteractions();
    }
}
