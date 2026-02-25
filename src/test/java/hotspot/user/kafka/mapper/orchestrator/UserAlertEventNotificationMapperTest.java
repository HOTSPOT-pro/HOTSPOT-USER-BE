package hotspot.user.kafka.mapper.orchestrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.mapper.strategy.appservice.AppServiceAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.strategy.policy.PolicyAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.strategy.present.PresentDataAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.strategy.usage.UsageThresholdAlertEventMappingStrategy;
import hotspot.user.notification.domain.Notification;

class UserAlertEventNotificationMapperTest {

    private final UserAlertEventNotificationMapper mapper = new UserAlertEventNotificationMapper(List.of(
            new UsageThresholdAlertEventMappingStrategy(),
            new PolicyAlertEventMappingStrategy(),
            new AppServiceAlertEventMappingStrategy(),
            new PresentDataAlertEventMappingStrategy()
    ));

    @Test
    @DisplayName("routes event type to mapped notification type")
    void mapRoutesByEventType() {
        UserAlertEvent event = event("USAGE_THRESHOLD", "PLAN_REMAINING", "50");

        assertThat(mapper.map(event).notificationType()).isEqualTo(NotificationType.SINGLE_USAGE_THRESHOLD_50);
    }

    @Test
    @DisplayName("same input produces same output")
    void sameInputSameOutput() {
        UserAlertEvent event = event("USAGE_THRESHOLD", "GIFT_REMAINING", "10");

        assertThat(mapper.map(event)).isEqualTo(mapper.map(event));
    }

    @Test
    @DisplayName("toNotification maps deterministic fields")
    void toNotificationSuccess() {
        Instant occurredAt = Instant.parse("2026-02-23T10:15:30Z");
        UserAlertEvent event = new UserAlertEvent(
                "alert-1",
                "USAGE_THRESHOLD",
                "PLAN_REMAINING",
                "30",
                null,
                null,
                null,
                null,
                occurredAt,
                101L,
                null,
                null,
                0L,
                30,
                "evt-100"
        );

        Notification notification = mapper.toNotification(event);

        assertThat(notification.getSubId()).isEqualTo(101L);
        assertThat(notification.getEventId()).isEqualTo("evt-100");
        assertThat(notification.getNotificationType()).isEqualTo("SINGLE_USAGE_THRESHOLD_30");
        assertThat(notification.getTitle()).isNotBlank();
        assertThat(notification.getContent()).contains("30%");
        assertThat(notification.getIsRead()).isFalse();
        assertThat(notification.getCreatedTime()).isEqualTo(LocalDateTime.of(2026, 2, 23, 10, 15, 30));
    }

    @Test
    @DisplayName("throws when event type is unsupported")
    void mapUnsupportedEventType() {
        UserAlertEvent event = event("UNKNOWN_EVENT", "ANY", "50");

        assertThatThrownBy(() -> mapper.map(event))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE.getMessage());
    }

    @Test
    @DisplayName("throws when subId is missing")
    void toNotificationWithoutSubId() {
        UserAlertEvent event = new UserAlertEvent(
                "alert-1",
                "USAGE_THRESHOLD",
                "PLAN_REMAINING",
                "30",
                null,
                null,
                null,
                null,
                Instant.parse("2026-02-23T10:15:30Z"),
                null,
                null,
                null,
                0L,
                30,
                "evt-100"
        );

        assertThatThrownBy(() -> mapper.toNotification(event))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.KAFKA_SUB_ID_REQUIRED.getMessage());
    }

    @Test
    @DisplayName("throws when subId is invalid")
    void toNotificationWithInvalidSubId() {
        UserAlertEvent event = new UserAlertEvent(
                "alert-1",
                "USAGE_THRESHOLD",
                "PLAN_REMAINING",
                "30",
                null,
                null,
                null,
                null,
                Instant.parse("2026-02-23T10:15:30Z"),
                0L,
                null,
                null,
                0L,
                30,
                "evt-100"
        );

        assertThatThrownBy(() -> mapper.toNotification(event))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.KAFKA_SUB_ID_INVALID.getMessage());
    }

    private UserAlertEvent event(String eventType, String alertType, String threshold) {
        return new UserAlertEvent(
                "alert-1",
                eventType,
                alertType,
                threshold,
                null,
                null,
                null,
                null,
                Instant.parse("2026-02-23T10:15:30Z"),
                100L,
                null,
                null,
                0L,
                10,
                "evt-1"
        );
    }
}
