package hotspot.user.dispatch.sms.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.notification.domain.Notification;

@ExtendWith(MockitoExtension.class)
class SmsDispatchQueuePublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SmsDispatchQueuePublisher publisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(publisher, "smsDispatchTopic", "sms-dispatch");
    }

    @Test
    @DisplayName("enqueues sms dispatch command payload to kafka topic")
    void enqueueSuccess() throws Exception {
        Notification notification = notification();
        given(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.any()))
                .willReturn("{\"notificationId\":1}");

        publisher.enqueue(notification);

        then(kafkaTemplate).should().send("sms-dispatch", "1:1", "{\"notificationId\":1}");
    }

    @Test
    @DisplayName("wraps exception with SMS_LISTENER_FAILED when enqueue fails")
    void enqueueFailure() throws Exception {
        Notification notification = notification();
        given(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.any()))
                .willThrow(new RuntimeException("serialize failed"));

        assertThatThrownBy(() -> publisher.enqueue(notification))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getCode()).isEqualTo(SmsErrorCode.SMS_LISTENER_FAILED);
                });

        then(kafkaTemplate).shouldHaveNoInteractions();
    }

    private Notification notification() {
        return Notification.builder()
                .id(1L)
                .subId(1L)
                .eventId("evt-1")
                .notificationType("SINGLE_USAGE_THRESHOLD_30")
                .title("title")
                .content("content")
                .isRead(false)
                .createdTime(LocalDateTime.of(2026, 2, 23, 10, 15, 30))
                .build();
    }
}
