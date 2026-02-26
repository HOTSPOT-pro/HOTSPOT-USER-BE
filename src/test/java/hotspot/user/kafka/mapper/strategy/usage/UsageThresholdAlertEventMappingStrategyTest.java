package hotspot.user.kafka.mapper.strategy.usage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;

class UsageThresholdAlertEventMappingStrategyTest {

    private final UsageThresholdAlertEventMappingStrategy strategy = new UsageThresholdAlertEventMappingStrategy();

    @Test
    @DisplayName("maps plan/family/gift threshold types")
    void mapsUsageThresholdTypes() {
        assertThat(strategy.map(event("PLAN_REMAINING", "50")).notificationType())
                .isEqualTo(NotificationType.SINGLE_USAGE_THRESHOLD_50);
        assertThat(strategy.map(event("FAMILY_POOL_REMAINING", "0")).notificationType())
                .isEqualTo(NotificationType.FAMILY_USAGE_EXHAUSTED);
        assertThat(strategy.map(event("GIFT_REMAINING", "10")).notificationType())
                .isEqualTo(NotificationType.PRESENT_USAGE_THRESHOLD_10);
    }

    @Test
    @DisplayName("throws when alert type is unsupported")
    void unsupportedAlertType() {
        assertThatThrownBy(() -> strategy.map(event("UNKNOWN_ALERT", "50")))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE.getMessage());
    }

    @Test
    @DisplayName("throws when threshold is unsupported")
    void unsupportedThreshold() {
        assertThatThrownBy(() -> strategy.map(event("PLAN_REMAINING", "77")))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD.getMessage());
    }

    private UserAlertEvent event(String alertType, String threshold) {
        return new UserAlertEvent(
                "alert-1",
                "USAGE_THRESHOLD",
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
