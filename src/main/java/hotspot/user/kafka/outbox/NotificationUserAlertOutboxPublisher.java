package hotspot.user.kafka.outbox;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import hotspot.user.kafka.domain.KafkaEventType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.outbox.service.NotificationOutboxEventAppender;
import hotspot.user.policy.domain.PolicyType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationUserAlertOutboxPublisher {

    private static final String ALERT_APPLIED = "APPLIED";
    private static final String ALERT_RELEASED = "RELEASED";
    private static final String ALERT_GIFTED = "GIFTED";
    private static final String aggregateType = "user-alert";

    private final NotificationOutboxEventAppender outboxEventAppender;

    // 정책 적용 알림 이벤트를 outbox에 적재한다.
    public void publishPolicyApplied(Long subId, Long familyId, String policyName, PolicyType policyType) {
        publishPolicyAlert(subId, familyId, policyName, policyType, ALERT_APPLIED, "POLICY_APPLIED");
    }

    // 정책 해제 알림 이벤트를 outbox에 적재한다.
    public void publishPolicyReleased(Long subId, Long familyId, String policyName, PolicyType policyType) {
        publishPolicyAlert(subId, familyId, policyName, policyType, ALERT_RELEASED, "POLICY_RELEASED");
    }

    // 서비스 접근 차단 알림 이벤트를 outbox에 적재한다.
    public void publishServiceAccessApplied(Long subId, Long familyId, String serviceName) {
        publishServiceAlert(subId, familyId, serviceName, ALERT_APPLIED, "SERVICE_ACCESS_APPLIED");
    }

    // 서비스 접근 해제 알림 이벤트를 outbox에 적재한다.
    public void publishServiceAccessReleased(Long subId, Long familyId, String serviceName) {
        publishServiceAlert(subId, familyId, serviceName, ALERT_RELEASED, "SERVICE_ACCESS_RELEASED");
    }

    // 데이터 선물 알림 이벤트를 outbox에 적재한다.
    public void publishPresentDataGifted(
            Long targetSubId,
            Long familyId,
            String senderName,
            String presentAmount,
            String giftId
    ) {
        UserAlertEvent event = baseEvent(targetSubId, familyId, KafkaEventType.PRESENT_DATA.name(), ALERT_GIFTED)
                .withPresent(senderName, presentAmount, giftId)
                .build();
        append("PRESENT_DATA_GIFTED", event);
    }

    // 정책 타입에 따라 이벤트 타입을 결정해 정책 알림 이벤트를 구성한다.
    private void publishPolicyAlert(
            Long subId,
            Long familyId,
            String policyName,
            PolicyType policyType,
            String alertType,
            String outboxType
    ) {
        String eventType = policyType == PolicyType.SCHEDULED
                ? KafkaEventType.TIME_WINDOW_POLICY.name()
                : KafkaEventType.IMMEDIATE_BLOCK.name();

        UserAlertEvent event = baseEvent(subId, familyId, eventType, alertType)
                .withPolicyName(policyName)
                .build();

        append(outboxType, event);
    }

    // 서비스 알림 이벤트를 공통 포맷으로 구성한다.
    private void publishServiceAlert(
            Long subId,
            Long familyId,
            String serviceName,
            String alertType,
            String outboxType
    ) {
        UserAlertEvent event = baseEvent(subId, familyId, KafkaEventType.SERVICE_ACCESS.name(), alertType)
                .withServiceName(serviceName)
                .build();
        append(outboxType, event);
    }

    // 집계 메타데이터와 함께 이벤트를 outbox_event 테이블에 저장한다.
    private void append(String type, UserAlertEvent event) {
        String aggregateId = event.subId() != null
                ? String.valueOf(event.subId())
                : event.familyId() != null ? String.valueOf(event.familyId()) : event.alertId();

        outboxEventAppender.append(
                aggregateType,
                aggregateId,
                type,
                event
        );
    }

    // 모든 알림 이벤트에서 공통으로 쓰는 기본 필드를 만든다.
    private UserAlertEventBuilder baseEvent(Long subId, Long familyId, String eventType, String alertType) {
        String eventId = UUID.randomUUID().toString();
        return new UserAlertEventBuilder(
                eventId,
                eventType,
                alertType,
                Instant.now(),
                subId,
                familyId
        );
    }

    private static class UserAlertEventBuilder {

        private final String alertId;
        private final String eventType;
        private final String alertType;
        private final Instant occurredAt;
        private final Long subId;
        private final Long familyId;
        private String policyName;
        private String serviceName;
        private String presentSenderName;
        private String presentAmount;
        private String giftId;

        UserAlertEventBuilder(
                String alertId,
                String eventType,
                String alertType,
                Instant occurredAt,
                Long subId,
                Long familyId
        ) {
            this.alertId = alertId;
            this.eventType = eventType;
            this.alertType = alertType;
            this.occurredAt = occurredAt;
            this.subId = subId;
            this.familyId = familyId;
        }

        // 정책명을 설정한다.
        UserAlertEventBuilder withPolicyName(String policyName) {
            this.policyName = policyName;
            return this;
        }

        // 서비스명을 설정한다.
        UserAlertEventBuilder withServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        // 선물 발신자명, 선물 용량, 선물 ID를 설정한다.
        UserAlertEventBuilder withPresent(String senderName, String amount, String giftId) {
            this.presentSenderName = senderName;
            this.presentAmount = amount;
            this.giftId = giftId;
            return this;
        }

        // 누적된 필드로 UserAlertEvent를 생성한다.
        UserAlertEvent build() {
            return new UserAlertEvent(
                    alertId,
                    eventType,
                    alertType,
                    null,
                    policyName,
                    serviceName,
                    presentSenderName,
                    presentAmount,
                    occurredAt,
                    subId,
                    familyId,
                    giftId,
                    0L,
                    0,
                    alertId
            );
        }
    }
}
