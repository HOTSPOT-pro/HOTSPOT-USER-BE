package hotspot.user.kafka.mapper.strategy.present;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

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
                100L,
                null,
                null,
                null,
                null,
                senderName,
                amount,
                null,
                null,
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
        );
    }
}
