package hotspot.user.outbox.consistencyOutbox.listener;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.OutboxErrorCode;
import hotspot.user.outbox.consistencyOutbox.domain.event.DomainEvent;
import hotspot.user.outbox.consistencyOutbox.infrastructure.OutboxEventRepository;

@ExtendWith(MockitoExtension.class)
class DomainEventOutboxListenerTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DomainEventOutboxListener listener;

    // 간단한 테스트용 DomainEvent 구현
    static class TestEvent implements DomainEvent {
        @Override
        public String type() {
            return "TEST_EVENT";
        }

        @Override
        public String aggregateType() {
            return "test";
        }

        @Override
        public String aggregateId() {
            return "1";
        }
    }

    @Test
    @DisplayName("성공: DomainEvent가 Outbox 테이블에 저장된다")
    void handleSuccess() throws Exception {

        // given
        TestEvent event = new TestEvent();

        given(objectMapper.writeValueAsString(event))
                .willReturn("{\"type\":\"TEST_EVENT\"}");

        // when
        listener.handle(event);

        // then
        verify(outboxEventRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("실패: JSON 직렬화 실패 시 예외가 발생한다")
    void handleSerializationFail() throws Exception {

        // given
        TestEvent event = new TestEvent();

        given(objectMapper.writeValueAsString(event))
                .willThrow(new RuntimeException("serialization fail"));

        // when & then
        assertThatThrownBy(() -> listener.handle(event))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(OutboxErrorCode.OUTBOX_PAYLOAD_SERIALIZATION_FAILED.getMessage());

        verify(outboxEventRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("실패: 저장 중 예외 발생 시 예외가 래핑되어 발생한다")
    void handleSaveFail() throws Exception {

        // given
        TestEvent event = new TestEvent();

        given(objectMapper.writeValueAsString(event))
                .willReturn("{\"type\":\"TEST_EVENT\"}");

        doThrow(new RuntimeException("db fail"))
                .when(outboxEventRepository)
                .save(any());

        // when & then
        assertThatThrownBy(() -> listener.handle(event))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(OutboxErrorCode.OUTBOX_EVENT_SAVE_FAILED.getMessage());
    }
}
