package hotspot.user.kafka.mapper.strategy.present;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;

class PresentDataAlertEventMappingStrategyTest {

    private final PresentDataAlertEventMappingStrategy strategy = new PresentDataAlertEventMappingStrategy();

    @Test
    @DisplayName("maps sender and amount as-is")
    void mapsSenderAndAmount() {
        UserAlertEvent event = event("Alice", "500MB");

        assertThat(strategy.map(event).notificationType()).isEqualTo(NotificationType.PRESENT_DATA);
        assertThat(strategy.map(event).content().body()).contains("Alice");
        assertThat(strategy.map(event).content().body()).contains("500MB");
    }

    @Test
    @DisplayName("falls back when sender or amount is blank")
    void fallbackValues() {
        UserAlertEvent event = event("", "");

        assertThat(strategy.map(event).content().body()).contains("0MB");
    }

    private UserAlertEvent event(String senderName, String amount) {
        return new UserAlertEvent(
                "alert-1",
                "PRESENT_DATA",
                null,
                null,
                null,
                null,
                senderName,
                amount,
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
