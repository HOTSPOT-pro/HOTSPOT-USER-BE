package hotspot.user.kafka.mapper.orchestrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.mapper.strategy.appservice.AppServiceAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.strategy.family.FamilyMemberApplyAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.strategy.policy.PolicyAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.strategy.present.PresentDataAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.strategy.usage.UsageThresholdAlertEventMappingStrategy;
import hotspot.user.notification.domain.Notification;

class UserAlertEventNotificationMapperTest {

    private final UserAlertEventNotificationMapper mapper = new UserAlertEventNotificationMapper(List.of(
            new UsageThresholdAlertEventMappingStrategy(),
            new PolicyAlertEventMappingStrategy(),
            new AppServiceAlertEventMappingStrategy(),
            new PresentDataAlertEventMappingStrategy(),
            new FamilyMemberApplyAlertEventMappingStrategy()
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
    @DisplayName("maps family member apply approved with targetName")
    void mapFamilyMemberApplyApproved() {
        UserAlertEvent event = new UserAlertEvent(
                "alert-2",
                "FAMILY_MEMBER_ADD",
                "APPROVED",
                null,
                200L,
                null,
                null,
                null,
                null,
                null,
                null,
                "Alice",
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
        );

        assertThat(mapper.map(event).notificationType())
                .isEqualTo(NotificationType.FAMILY_MEMBER_ADD_APPROVED);
        assertThat(mapper.map(event).content().body()).contains("Alice");
    }

    @Test
    @DisplayName("toNotification maps deterministic fields")
    void toNotificationSuccess() {
        LocalDateTime createdTime = LocalDateTime.of(2026, 2, 23, 10, 15, 30);
        UserAlertEvent event = new UserAlertEvent(
                "alert-1",
                "USAGE_THRESHOLD",
                "PLAN_REMAINING",
                101L,
                null,
                "30",
                null,
                null,
                null,
                null,
                null,
                null,
                createdTime
        );

        Notification notification = mapper.toNotification(event);

        assertThat(notification.getSubId()).isEqualTo(101L);
        assertThat(notification.getEventId()).isEqualTo("alert-1");
        assertThat(notification.getNotificationType()).isEqualTo("SINGLE_USAGE_THRESHOLD_30");
        assertThat(notification.getTitle()).isNotBlank();
        assertThat(notification.getContent()).contains("30%");
        assertThat(notification.getIsRead()).isFalse();
        assertThat(notification.getCreatedTime()).isEqualTo(createdTime);
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
                null,
                null,
                "30",
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
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
                0L,
                null,
                "30",
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
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
                100L,
                null,
                threshold,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
        );
    }
}
