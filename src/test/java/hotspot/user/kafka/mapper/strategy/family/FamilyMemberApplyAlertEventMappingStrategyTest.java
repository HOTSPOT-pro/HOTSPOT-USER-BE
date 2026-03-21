package hotspot.user.kafka.mapper.strategy.family;

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

class FamilyMemberApplyAlertEventMappingStrategyTest {

    private final FamilyMemberApplyAlertEventMappingStrategy strategy =
            new FamilyMemberApplyAlertEventMappingStrategy();

    @Test
    @DisplayName("maps add approved and rejected apply result")
    void mapsAddApprovedAndRejected() {
        UserAlertEvent approved = event("FAMILY_MEMBER_ADD", "APPROVED", List.of("Alice"));
        UserAlertEvent rejected = event("FAMILY_MEMBER_ADD", "REJECTED", List.of("Bob"));

        assertThat(strategy.map(approved).notificationType())
                .isEqualTo(NotificationType.FAMILY_MEMBER_ADD_APPROVED);
        assertThat(strategy.map(approved).content().body()).contains("Alice");
        assertThat(strategy.map(rejected).notificationType())
                .isEqualTo(NotificationType.FAMILY_MEMBER_ADD_REJECTED);
        assertThat(strategy.map(rejected).content().body()).contains("Bob");
    }

    @Test
    @DisplayName("maps remove approved and rejected apply result")
    void mapsRemoveApprovedAndRejected() {
        UserAlertEvent approved = event("FAMILY_MEMBER_REMOVE", "APPROVED", List.of("Alice"));
        UserAlertEvent rejected = event("FAMILY_MEMBER_REMOVE", "REJECTED", List.of("Bob"));

        assertThat(strategy.map(approved).notificationType())
                .isEqualTo(NotificationType.FAMILY_MEMBER_REMOVE_APPROVED);
        assertThat(strategy.map(approved).content().body()).contains("Alice");
        assertThat(strategy.map(rejected).notificationType())
                .isEqualTo(NotificationType.FAMILY_MEMBER_REMOVE_REJECTED);
        assertThat(strategy.map(rejected).content().body()).contains("Bob");
    }

    @Test
    @DisplayName("maps family create approved and rejected apply result")
    void mapsFamilyCreateApprovedAndRejected() {
        UserAlertEvent approved = event("FAMILY_CREATE", "APPROVED", List.of("Smith"));
        UserAlertEvent rejected = event("FAMILY_CREATE", "REJECTED", List.of("Smith"));

        assertThat(strategy.map(approved).notificationType())
                .isEqualTo(NotificationType.FAMILY_CREATE_APPROVED);
        assertThat(strategy.map(rejected).notificationType())
                .isEqualTo(NotificationType.FAMILY_CREATE_REJECTED);
    }

    @Test
    @DisplayName("throws on unsupported alert type")
    void unsupportedType() {
        assertThatThrownBy(() -> strategy.map(event("FAMILY_MEMBER_ADD", "PENDING", List.of("Chris"))))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE.getMessage());
    }

    private UserAlertEvent event(String eventType, String alertType, List<String> targetNames) {
        return new UserAlertEvent(
                "alert-family",
                eventType,
                alertType,
                null,
                200L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                targetNames,
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
        );
    }
}
