package hotspot.user.outbox.notificationOutbox.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.OutboxErrorCode;
import hotspot.user.kafka.domain.KafkaEventType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.outbox.notificationOutbox.domain.event.AlertAction;
import hotspot.user.outbox.notificationOutbox.domain.event.PolicyAlertOutboxEvent;
import hotspot.user.outbox.notificationOutbox.domain.event.PresentDataGiftedOutboxEvent;
import hotspot.user.outbox.notificationOutbox.domain.event.ServiceAccessAlertOutboxEvent;
import hotspot.user.outbox.notificationOutbox.service.port.UserAlertNotificationOutboxPort;
import hotspot.user.policy.domain.PolicyType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAlertNotificationOutboxService implements UserAlertNotificationOutboxPort {

    private static final String AGGREGATE_TYPE = "user-alert";

    private final NotificationOutboxEventAppender outboxEventAppender;

    // 정책 적용/해제 이벤트를 UserAlertEvent로 변환해 Outbox에 적재한다.
    @Override
    public void appendPolicyAlert(PolicyAlertOutboxEvent event) {
        String eventType = event.policyType() == PolicyType.SCHEDULED
                ? KafkaEventType.TIME_WINDOW_POLICY.name()
                : KafkaEventType.IMMEDIATE_BLOCK.name();

        UserAlertEvent payload = baseEvent(event.subId(), event.familyId(), eventType, event.action().name())
                .withPolicyName(event.policyName())
                .build();

        append(eventType, payload);
    }

    // 앱/서비스 차단(접근) 적용/해제 이벤트를 알림 이벤트로 만들어 Outbox에 적재한다.
    @Override
    public void appendServiceAccessAlert(ServiceAccessAlertOutboxEvent event) {
        String eventType = KafkaEventType.SERVICE_ACCESS.name();

        UserAlertEvent payload = baseEvent(
                event.subId(),
                event.familyId(),
                eventType,
                event.action().name()
        )
                .withServiceName(event.serviceName())
                .build();

        append(eventType, payload);
    }

    // 데이터 선물(지급) 이벤트를 알림 이벤트로 만들어 Outbox에 적재한다.
    @Override
    public void appendPresentDataGiftedAlert(PresentDataGiftedOutboxEvent event) {
        String eventType = KafkaEventType.PRESENT_DATA.name();
        UserAlertEvent payload = baseEvent(
                event.targetSubId(),
                event.familyId(),
                eventType,
                AlertAction.GIFTED.name()
        )
                .withPresent(event.senderName(), event.presentAmount(), event.giftId())
                .build();
        append(eventType, payload);
    }

    // subId/familyId 기반으로 aggregateId를 결정해 Outbox에 저장하고 예외를 래핑 처리한다.
    private void append(String type, UserAlertEvent event) {
        try {
            String aggregateId;
            if (event.subId() != null) {
                aggregateId = String.valueOf(event.subId());
            } else if (event.familyId() != null) {
                aggregateId = String.valueOf(event.familyId());
            } else {
                aggregateId = event.alertId();
            }

            outboxEventAppender.append(
                    AGGREGATE_TYPE,
                    aggregateId,
                    type,
                    event
            );
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException(OutboxErrorCode.OUTBOX_EVENT_PUBLISH_FAILED, ex);
        }
    }

    // 공통 필드(eventId, 타입, 생성시간 등)를 채운 UserAlertEventBuilder를 생성한다.
    private UserAlertEventBuilder baseEvent(Long subId, Long familyId, String eventType, String alertType) {
        String eventId = UUID.randomUUID().toString();
        return new UserAlertEventBuilder(
                eventId,
                eventType,
                alertType,
                LocalDateTime.now(ZoneOffset.UTC),
                subId,
                familyId
        );
    }

    // 알림 이벤트 생성에 필요한 기본 정보들을 builder에 세팅한다.
    private static class UserAlertEventBuilder {

        private final String alertId;
        private final String eventType;
        private final String alertType;
        private final LocalDateTime createdTime;
        private final Long subId;
        private final Long familyId;
        private String threshold;
        private String policyName;
        private String serviceName;
        private String presentSenderName;
        private String presentAmount;
        private String giftId;

        UserAlertEventBuilder(
                String alertId,
                String eventType,
                String alertType,
                LocalDateTime createdTime,
                Long subId,
                Long familyId
        ) {
            this.alertId = alertId;
            this.eventType = eventType;
            this.alertType = alertType;
            this.createdTime = createdTime;
            this.subId = subId;
            this.familyId = familyId;
        }

        // 정책명 필드를 builder에 설정한다.
        UserAlertEventBuilder withPolicyName(String policyName) {
            this.policyName = policyName;
            return this;
        }

        // 서비스명 필드를 builder에 설정한다.
        UserAlertEventBuilder withServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        // 선물 정보(발신자/수량/선물ID)를 builder에 설정한다.
        UserAlertEventBuilder withPresent(String senderName, String amount, String giftId) {
            this.presentSenderName = senderName;
            this.presentAmount = amount;
            this.giftId = giftId;
            return this;
        }

        UserAlertEvent build() {
            return new UserAlertEvent(
                    alertId,
                    eventType,
                    alertType,
                    subId,
                    familyId,
                    threshold,
                    policyName,
                    serviceName,
                    presentSenderName,
                    presentAmount,
                    giftId,
                    null,
                    createdTime
            );
        }
    }
}
