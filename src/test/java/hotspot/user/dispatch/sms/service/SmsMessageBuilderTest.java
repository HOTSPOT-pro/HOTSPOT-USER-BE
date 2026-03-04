package hotspot.user.dispatch.sms.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.dispatch.sms.config.SmsProperties;
import hotspot.user.notification.domain.Notification;

class SmsMessageBuilderTest {

    @Test
    @DisplayName("builds sms message with from and notification content")
    void buildMessage() {
        SmsMessageBuilder builder = new SmsMessageBuilder(
                new SmsProperties(true, false, false, false, false, "noop", "HOTSPOT")
        );
        Notification notification = Notification.builder()
                .id(1L)
                .subId(1L)
                .eventId("evt-1")
                .notificationType("SINGLE_USAGE_THRESHOLD_50")
                .title("title")
                .content("content")
                .isRead(false)
                .createdTime(LocalDateTime.of(2026, 2, 23, 10, 15, 30))
                .build();

        String message = builder.build(notification);

        assertThat(message).isEqualTo("[HOTSPOT] title - content");
    }
}
