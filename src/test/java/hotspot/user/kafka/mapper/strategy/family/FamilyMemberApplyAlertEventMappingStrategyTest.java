package hotspot.user.kafka.mapper.strategy.family;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;

class FamilyMemberApplyAlertEventMappingStrategyTest {

    private final FamilyMemberApplyAlertEventMappingStrategy strategy =
            new FamilyMemberApplyAlertEventMappingStrategy();

    @Test
    @DisplayName("maps approved and rejected apply result")
    void mapsApprovedAndRejected() {
        UserAlertEvent approved = event("APPROVED", "Alice");
        UserAlertEvent rejected = event("REJECTED", "Bob");

        assertThat(strategy.map(approved).notificationType())
                .isEqualTo(NotificationType.FAMILY_MEMBER_ADD_APPROVED);
        assertThat(strategy.map(approved).content().body()).contains("Alice");
        assertThat(strategy.map(rejected).notificationType())
                .isEqualTo(NotificationType.FAMILY_MEMBER_ADD_REJECTED);
        assertThat(strategy.map(rejected).content().body()).contains("Bob");
    }

    @Test
    @DisplayName("throws on unsupported alert type")
    void unsupportedType() {
        assertThatThrownBy(() -> strategy.map(event("PENDING", "Chris")))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE.getMessage());
    }

    private UserAlertEvent event(String alertType, String targetName) {
        return new UserAlertEvent(
                "alert-family",
                "FAMILY_MEMBER_ADD",
                alertType,
                null,
                200L,
                null,
                null,
                null,
                null,
                null,
                null,
                targetName,
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
        );
    }
}
