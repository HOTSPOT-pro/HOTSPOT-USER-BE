package hotspot.user.kafka.mapper.strategy.policy;

import org.springframework.stereotype.Component;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.KafkaEventType;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.mapper.strategy.UserAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.support.AlertEventMappingSupport;
import hotspot.user.kafka.mapper.template.AlertMessageTemplateRegistry;
import hotspot.user.kafka.model.AlertNotificationMappingResult;

@Component
public class PolicyAlertEventMappingStrategy implements UserAlertEventMappingStrategy {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return eventType == KafkaEventType.TIME_WINDOW_POLICY
                || eventType == KafkaEventType.IMMEDIATE_BLOCK;
    }

    @Override
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        KafkaEventType eventType = KafkaEventType.from(AlertEventMappingSupport.normalize(event.eventType()));
        return switch (eventType) {
            case TIME_WINDOW_POLICY -> mapTimeWindowPolicy(event);
            case IMMEDIATE_BLOCK -> mapImmediateBlock(event);
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        };
    }

    private AlertNotificationMappingResult mapTimeWindowPolicy(UserAlertEvent event) {
        String normalizedAlertType = AlertEventMappingSupport.normalize(event.alertType());
        String policyName = AlertEventMappingSupport.defaultIfBlank(event.policyName(), "정책");

        return switch (normalizedAlertType) {
            case "APPLIED" -> AlertMessageTemplateRegistry.create(
                    NotificationType.TIME_WINDOW_POLICY_APPLIED,
                    policyName
            );
            case "RELEASED" -> AlertMessageTemplateRegistry.create(
                    NotificationType.TIME_WINDOW_POLICY_RELEASED,
                    policyName
            );
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
        };
    }

    private AlertNotificationMappingResult mapImmediateBlock(UserAlertEvent event) {
        String normalizedAlertType = AlertEventMappingSupport.normalize(event.alertType());

        return switch (normalizedAlertType) {
            case "APPLIED" -> AlertMessageTemplateRegistry.create(NotificationType.IMMEDIATE_BLOCK_APPLIED);
            case "RELEASED" -> AlertMessageTemplateRegistry.create(NotificationType.IMMEDIATE_BLOCK_RELEASED);
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
        };
    }
}
