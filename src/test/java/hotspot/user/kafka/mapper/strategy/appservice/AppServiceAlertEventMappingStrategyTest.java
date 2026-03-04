package hotspot.user.kafka.mapper.strategy.appservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;

class AppServiceAlertEventMappingStrategyTest {

    private final AppServiceAlertEventMappingStrategy strategy = new AppServiceAlertEventMappingStrategy();

    @Test
    @DisplayName("maps applied and released service events")
    void mapsServiceAccessEvents() {
        UserAlertEvent blocked = event("APPLIED", "YouTube");
        UserAlertEvent unblocked = event("RELEASED", "YouTube");

        assertThat(strategy.map(blocked).notificationType()).isEqualTo(NotificationType.SERVICE_ACCESS_BLOCKED);
        assertThat(strategy.map(blocked).content().body()).contains("YouTube");
        assertThat(strategy.map(unblocked).notificationType()).isEqualTo(NotificationType.SERVICE_ACCESS_RELEASED);
    }

    @Test
    @DisplayName("throws when alert type is unsupported")
    void unsupportedAlertType() {
        assertThatThrownBy(() -> strategy.map(event("UNKNOWN", "YouTube")))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE.getMessage());
    }

    private UserAlertEvent event(String alertType, String serviceName) {
        return new UserAlertEvent(
                "alert-1",
                "SERVICE_ACCESS",
                alertType,
                100L,
                null,
                null,
                null,
                null,
                null,
                null,
                serviceName,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
        );
    }
}
