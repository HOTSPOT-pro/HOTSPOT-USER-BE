package hotspot.user.dispatch.sms.listener;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.dispatch.sms.dto.SmsDispatchCommand;
import hotspot.user.dispatch.sms.service.SmsDispatchService;
import hotspot.user.notification.domain.Notification;

@ExtendWith(MockitoExtension.class)
class SmsDispatchConsumerTest {

    @Mock
    private SmsDispatchService smsDispatchService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private SmsDispatchConsumer smsDispatchConsumer;

    @Test
    @DisplayName("acks when SMS dispatch succeeds")
    void ackWhenDispatchSucceeds() {
        SmsDispatchCommand command = command(1L, 100L);

        smsDispatchConsumer.consume(command, acknowledgment);

        then(smsDispatchService).should().dispatch(any(Notification.class));
        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("acks and skips when non-retryable application exception occurs")
    void ackWhenNonRetryableErrorOccurs() {
        SmsDispatchCommand command = command(1L, 100L);
        willThrow(new ApplicationException(SmsErrorCode.SMS_NOTIFICATION_ALLOW_DISABLED))
                .given(smsDispatchService)
                .dispatch(any(Notification.class));

        smsDispatchConsumer.consume(command, acknowledgment);

        then(acknowledgment).should().acknowledge();
    }

    @Test
    @DisplayName("throws to retry when retryable application exception occurs")
    void throwsWhenRetryableErrorOccurs() {
        SmsDispatchCommand command = command(1L, 100L);
        willThrow(new ApplicationException(SmsErrorCode.SMS_DISPATCH_FAILED))
                .given(smsDispatchService)
                .dispatch(any(Notification.class));

        assertThatThrownBy(() -> smsDispatchConsumer.consume(command, acknowledgment))
                .isInstanceOf(ApplicationException.class);

        then(acknowledgment).should(never()).acknowledge();
    }

    private SmsDispatchCommand command(Long notificationId, Long subId) {
        return new SmsDispatchCommand(
                notificationId,
                subId,
                "event-1",
                "SINGLE_USAGE_THRESHOLD_30",
                "title",
                "content",
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
        );
    }
}
