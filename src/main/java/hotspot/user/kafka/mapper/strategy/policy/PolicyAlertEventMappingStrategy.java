package hotspot.user.kafka.mapper.strategy.policy;

import org.springframework.stereotype.Component;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.KafkaEventType;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.mapper.strategy.UserAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.support.AlertEventMappingSupport;
import hotspot.user.kafka.model.AlertNotificationMappingResult;

@Component
public class PolicyAlertEventMappingStrategy implements UserAlertEventMappingStrategy {

    // 이 전략이 TIME_WINDOW_POLICY 또는 IMMEDIATE_BLOCK 이벤트 타입을 처리하는지 여부를 반환한다.
    @Override
    public boolean supports(KafkaEventType eventType) {
        return eventType == KafkaEventType.TIME_WINDOW_POLICY
                || eventType == KafkaEventType.IMMEDIATE_BLOCK;
    }

    // 이벤트의 eventType을 변환한 뒤, 타입에 따라 시간 차단 정책/즉시 차단 정책 매핑으로 분기한다.
    @Override
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        KafkaEventType eventType = KafkaEventType.from(AlertEventMappingSupport.normalize(event.eventType()));
        return switch (eventType) {
            case TIME_WINDOW_POLICY -> mapTimeWindowPolicy(event);
            case IMMEDIATE_BLOCK -> mapImmediateBlock(event);
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        };
    }

    // alertType과 policyName을 정규화해 시간 차단 정책의 적용/해제 알림 메시지를 생성한다.
    private AlertNotificationMappingResult mapTimeWindowPolicy(UserAlertEvent event) {
        String normalizedAlertType = AlertEventMappingSupport.normalize(event.alertType());
        String policyName = AlertEventMappingSupport.defaultIfBlank(event.policyName(), "정책");

        return switch (normalizedAlertType) {
            case "APPLIED" -> AlertEventMappingSupport.create(
                    NotificationType.TIME_WINDOW_POLICY_APPLIED,
                    "시간 차단 정책 알림",
                    "\"" + policyName + "\" 시간 차단 정책이 적용되었습니다."
            );
            case "RELEASED" -> AlertEventMappingSupport.create(
                    NotificationType.TIME_WINDOW_POLICY_RELEASED,
                    "시간 차단 정책 알림",
                    "\"" + policyName + "\" 시간 차단 정책이 해제되었습니다."
            );
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
        };
    }

    // alertType을 정규화해 즉시 차단 정책의 적용/해제 알림 메시지를 생성한다.
    private AlertNotificationMappingResult mapImmediateBlock(UserAlertEvent event) {
        String normalizedAlertType = AlertEventMappingSupport.normalize(event.alertType());

        return switch (normalizedAlertType) {
            case "APPLIED" -> AlertEventMappingSupport.create(
                    NotificationType.IMMEDIATE_BLOCK_APPLIED,
                    "즉시 차단 정책 알림",
                    "데이터 사용 차단이 즉시 적용되었습니다."
            );
            case "RELEASED" -> AlertEventMappingSupport.create(
                    NotificationType.IMMEDIATE_BLOCK_RELEASED,
                    "즉시 차단 정책 알림",
                    "데이터 사용 차단이 해제되었습니다."
            );
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
        };
    }
}
