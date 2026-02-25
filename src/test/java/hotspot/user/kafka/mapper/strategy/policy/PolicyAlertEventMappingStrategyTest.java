package hotspot.user.kafka.mapper.strategy.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;

class PolicyAlertEventMappingStrategyTest {

    private final PolicyAlertEventMappingStrategy strategy = new PolicyAlertEventMappingStrategy();

    @Test
    @DisplayName("maps time window policy applied/released")
    void mapsTimeWindowPolicy() {
        UserAlertEvent applied = event("TIME_WINDOW_POLICY", "APPLIED", "night");
        UserAlertEvent released = event("TIME_WINDOW_POLICY", "RELEASED", "night");

        assertThat(strategy.map(applied).notificationType()).isEqualTo(NotificationType.TIME_WINDOW_POLICY_APPLIED);
        assertThat(strategy.map(applied).content().body()).contains("night");
        assertThat(strategy.map(released).notificationType()).isEqualTo(NotificationType.TIME_WINDOW_POLICY_RELEASED);
    }

    @Test
    @DisplayName("maps immediate block applied/released")
    void mapsImmediateBlock() {
        UserAlertEvent applied = event("IMMEDIATE_BLOCK", "APPLIED", null);
        UserAlertEvent released = event("IMMEDIATE_BLOCK", "RELEASED", null);

        assertThat(strategy.map(applied).notificationType()).isEqualTo(NotificationType.IMMEDIATE_BLOCK_APPLIED);
        assertThat(strategy.map(released).notificationType()).isEqualTo(NotificationType.IMMEDIATE_BLOCK_RELEASED);
    }

    @Test
    @DisplayName("throws when alert type is unsupported")
    void unsupportedAlertType() {
        assertThatThrownBy(() -> strategy.map(event("TIME_WINDOW_POLICY", "UNKNOWN", null)))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE.getMessage());
    }

    private UserAlertEvent event(String eventType, String alertType, String policyName) {
        return new UserAlertEvent(
                "alert-1",
                eventType,
                alertType,
                null,
                policyName,
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
