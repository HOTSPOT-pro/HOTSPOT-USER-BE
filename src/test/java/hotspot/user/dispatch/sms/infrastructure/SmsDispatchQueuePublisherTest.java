package hotspot.user.dispatch.sms.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import hotspot.user.dispatch.sms.service.SmsDispatchQueuePublisher;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.notification.domain.Notification;
import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class SmsDispatchQueuePublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PresentDataRepository presentDataRepository;

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
        UserAlertEvent sourceEvent = sourceEvent();
        given(subscriptionRepository.findById(1L))
                .willReturn(Optional.of(Subscription.builder()
                        .id(1L)
                        .plan(hotspot.user.plan.domain.Plan.builder().id(10L).name("유쓰 5G 데이터 플러스").build())
                        .build()));
        given(presentDataRepository.findGiftGiverNames(List.of(777L)))
                .willReturn(Map.of(777L, "민수"));
        given(objectMapper.writeValueAsString(any()))
                .willReturn("{\"notificationId\":1}");
        SmsDispatchQueuePublisher.SmsDispatchMetadata metadata = publisher.resolveMetadata(sourceEvent);

        publisher.enqueue(notification, sourceEvent, metadata);

        then(kafkaTemplate).should().send("sms-dispatch", "1:1", "{\"notificationId\":1}");
        then(subscriptionRepository).should().findById(1L);
        then(presentDataRepository).should().findGiftGiverNames(List.of(777L));
    }

    @Test
    @DisplayName("wraps exception with SMS_LISTENER_FAILED when enqueue fails")
    void enqueueFailure() throws Exception {
        Notification notification = notification();
        UserAlertEvent sourceEvent = sourceEvent();
        given(subscriptionRepository.findById(1L)).willReturn(Optional.empty());
        given(presentDataRepository.findGiftGiverNames(List.of(777L))).willReturn(Map.of());
        given(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.any()))
                .willThrow(new RuntimeException("serialize failed"));
        SmsDispatchQueuePublisher.SmsDispatchMetadata metadata = publisher.resolveMetadata(sourceEvent);

        assertThatThrownBy(() -> publisher.enqueue(notification, sourceEvent, metadata))
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

    private UserAlertEvent sourceEvent() {
        return new UserAlertEvent(
                "alert-1",
                "USAGE_THRESHOLD",
                "PLAN_REMAINING",
                1L,
                null,
                "30",
                "110GB",
                "80%",
                "88.02GB",
                null,
                null,
                "existing-sender",
                null,
                "777",
                null,
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
        );
    }
}
