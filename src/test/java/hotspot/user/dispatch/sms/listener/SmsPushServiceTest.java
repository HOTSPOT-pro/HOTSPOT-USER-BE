package hotspot.user.dispatch.sms.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.dispatch.sms.config.SmsProperties;
import hotspot.user.dispatch.sms.service.SmsDispatchQueuePublisher;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.dto.UserAlertNotificationsPersistedEvent;
import hotspot.user.notification.domain.Notification;

@ExtendWith(MockitoExtension.class)
class SmsPushServiceTest {

    @Mock
    private SmsDispatchQueuePublisher smsDispatchQueuePublisher;

    @Mock
    private SmsProperties smsProperties;

    @InjectMocks
    private SmsPushService smsPushService;

    @Test
    @DisplayName("enqueues notifications when sms is enabled")
    void dispatchesWhenEnabled() {
        mockEnabled();
        Notification notification = notification(1L, "IMMEDIATE_BLOCK_APPLIED");
        UserAlertNotificationsPersistedEvent event = new UserAlertNotificationsPersistedEvent(
                sourceEvent(),
                List.of(notification)
        );

        smsPushService.onNotificationsPersisted(event);

        then(smsDispatchQueuePublisher).should().enqueue(notification, event.sourceEvent());
    }

    @Test
    @DisplayName("does not enqueue notifications when sms is disabled")
    void doesNotDispatchWhenDisabled() {
        given(smsProperties.isEnabled()).willReturn(false);
        Notification notification = notification(1L, "IMMEDIATE_BLOCK_APPLIED");
        UserAlertNotificationsPersistedEvent event = new UserAlertNotificationsPersistedEvent(
                sourceEvent(),
                List.of(notification)
        );

        smsPushService.onNotificationsPersisted(event);

        then(smsDispatchQueuePublisher).should(never()).enqueue(notification, event.sourceEvent());
    }

    @Test
    @DisplayName("throws listener error when enqueue fails")
    void throwsWhenEnqueueFails() {
        mockEnabled();
        Notification first = notification(1L, "IMMEDIATE_BLOCK_APPLIED");
        UserAlertNotificationsPersistedEvent event = new UserAlertNotificationsPersistedEvent(
                sourceEvent(),
                List.of(first)
        );
        doThrow(new RuntimeException("unexpected")).when(smsDispatchQueuePublisher).enqueue(first, event.sourceEvent());

        assertThatThrownBy(() -> smsPushService.onNotificationsPersisted(event))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getCode()).isEqualTo(SmsErrorCode.SMS_LISTENER_FAILED);
                });

        then(smsDispatchQueuePublisher).should().enqueue(first, event.sourceEvent());
    }

    private void mockEnabled() {
        given(smsProperties.isEnabled()).willReturn(true);
    }

    private UserAlertEvent sourceEvent() {
        return new UserAlertEvent(
                "alert-1",
                "USAGE_THRESHOLD",
                "PLAN_REMAINING",
                1L,
                null,
                "30",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
        );
    }

    private Notification notification(Long subId, String type) {
        return Notification.builder()
                .id(1L)
                .subId(subId)
                .eventId("evt-1")
                .notificationType(type)
                .title("title-1")
                .content("content-1")
                .isRead(false)
                .createdTime(LocalDateTime.of(2026, 2, 23, 10, 15, 30))
                .build();
    }
}
