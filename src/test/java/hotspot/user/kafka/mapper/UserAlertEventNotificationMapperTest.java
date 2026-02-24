package hotspot.user.kafka.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.model.AlertNotificationMappingResult;
import hotspot.user.notification.domain.Notification;

class UserAlertEventNotificationMapperTest {

    private final UserAlertEventNotificationMapper mapper = new UserAlertEventNotificationMapper();

    @Test
    @DisplayName("maps Lua usage threshold for plan remaining")
    // 개인 요금제 임계치 매핑 결과를 검증한다.
    void mapLuaPlanRemaining() {
        UserAlertEvent event = event("USAGE_THRESHOLD", "PLAN_REMAINING", "50", null, null, null, null);

        AlertNotificationMappingResult result = mapper.map(event);

        assertThat(result.notificationType()).isEqualTo(NotificationType.SINGLE_USAGE_THRESHOLD_50);
        assertThat(result.content().body()).contains("50%");
    }

    @Test
    @DisplayName("maps Lua usage threshold for family pool")
    // 가족 공유 임계치 매핑 결과를 검증한다.
    void mapLuaFamilyPoolRemaining() {
        UserAlertEvent event = event("USAGE_THRESHOLD", "FAMILY_POOL_REMAINING", "0", null, null, null, null);

        AlertNotificationMappingResult result = mapper.map(event);

        assertThat(result.notificationType()).isEqualTo(NotificationType.FAMILY_USAGE_EXHAUSTED);
        assertThat(result.content().body()).contains("소진");
    }

    @Test
    @DisplayName("maps Lua usage threshold for gift remaining")
    // 선물 데이터 임계치 매핑 결과를 검증한다.
    void mapLuaGiftRemaining() {
        UserAlertEvent event = event("USAGE_THRESHOLD", "GIFT_REMAINING", "10", null, null, null, null);

        AlertNotificationMappingResult result = mapper.map(event);

        assertThat(result.notificationType()).isEqualTo(NotificationType.PRESENT_USAGE_THRESHOLD_10);
        assertThat(result.content().body()).contains("10%");
    }

    @Test
    @DisplayName("maps present data amount as-is")
    // 선물 데이터 알림 문구에 용량이 반영되는지 검증한다.
    void mapPresentData() {
        UserAlertEvent event = event("PRESENT_DATA", null, null, null, null, "홍길동", "500MB");

        AlertNotificationMappingResult result = mapper.map(event);

        assertThat(result.notificationType()).isEqualTo(NotificationType.PRESENT_DATA);
        assertThat(result.content().body()).contains("500MB");
    }

    @Test
    @DisplayName("maps policy and service events")
    // 정책/서비스 이벤트 매핑 결과를 검증한다.
    void mapPolicyAndServiceEvents() {
        UserAlertEvent timeApplied = event("TIME_WINDOW_POLICY", "APPLIED", null, "야간 차단", null, null, null);
        UserAlertEvent immediateReleased = event("IMMEDIATE_BLOCK", "RELEASED", null, null, null, null, null);
        UserAlertEvent serviceBlocked = event("SERVICE_ACCESS", "BLOCKED", null, null, "YouTube", null, null);

        AlertNotificationMappingResult timeResult = mapper.map(timeApplied);
        AlertNotificationMappingResult immediateResult = mapper.map(immediateReleased);
        AlertNotificationMappingResult serviceResult = mapper.map(serviceBlocked);

        assertThat(timeResult.notificationType()).isEqualTo(NotificationType.TIME_WINDOW_POLICY_APPLIED);
        assertThat(timeResult.content().body()).contains("야간 차단");
        assertThat(immediateResult.notificationType()).isEqualTo(NotificationType.IMMEDIATE_BLOCK_RELEASED);
        assertThat(serviceResult.notificationType()).isEqualTo(NotificationType.SERVICE_ACCESS_BLOCKED);
    }

    @Test
    @DisplayName("same input always produces same output")
    // 동일 입력에 대해 같은 매핑 결과가 나오는지 검증한다.
    void sameInputSameOutput() {
        UserAlertEvent event = event("USAGE_THRESHOLD", "GIFT_REMAINING", "10", null, null, null, null);

        AlertNotificationMappingResult first = mapper.map(event);
        AlertNotificationMappingResult second = mapper.map(event);

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("toNotification maps deterministic fields")
    // Notification 변환 시 핵심 필드 매핑을 검증한다.
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
        assertThat(notification.getContent()).contains("30%");
        assertThat(notification.getIsRead()).isFalse();
        assertThat(notification.getCreatedTime()).isEqualTo(LocalDateTime.of(2026, 2, 23, 10, 15, 30));
    }

    @Test
    @DisplayName("throws common exception when event type is unsupported")
    // 지원하지 않는 eventType이면 예외가 나는지 검증한다.
    void mapUnsupportedEventType() {
        UserAlertEvent event = event("UNKNOWN_EVENT", "ANY", "50", null, null, null, null);

        assertThatThrownBy(() -> mapper.map(event))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE.getMessage());
    }

    @Test
    @DisplayName("throws common exception when alert type is unsupported")
    // 지원하지 않는 alertType이면 예외가 나는지 검증한다.
    void mapUnsupportedAlertType() {
        UserAlertEvent event = event("USAGE_THRESHOLD", "UNKNOWN_ALERT", "50", null, null, null, null);

        assertThatThrownBy(() -> mapper.map(event))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE.getMessage());
    }

    @Test
    @DisplayName("throws common exception when threshold is unsupported")
    // 지원하지 않는 threshold이면 예외가 나는지 검증한다.
    void mapUnsupportedThreshold() {
        UserAlertEvent event = event("USAGE_THRESHOLD", "PLAN_REMAINING", "77", null, null, null, null);

        assertThatThrownBy(() -> mapper.map(event))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD.getMessage());
    }

    @Test
    @DisplayName("throws common exception when subId is missing")
    // subId가 없을 때 예외가 나는지 검증한다.
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
    @DisplayName("throws common exception when subId is invalid")
    // subId가 0 이하일 때 예외가 나는지 검증한다.
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

    // 테스트용 UserAlertEvent 객체를 생성한다.
    private UserAlertEvent event(
            String eventType,
            String alertType,
            String threshold,
            String policyName,
            String serviceName,
            String senderName,
            String amount
    ) {
        return new UserAlertEvent(
                "alert-1",
                eventType,
                alertType,
                threshold,
                policyName,
                serviceName,
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
