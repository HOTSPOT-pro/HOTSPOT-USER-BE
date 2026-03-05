package hotspot.user.kafka.mapper.strategy.appservice;

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
public class AppServiceAlertEventMappingStrategy implements UserAlertEventMappingStrategy {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return eventType == KafkaEventType.SERVICE_ACCESS;
    }

    @Override
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        String normalizedAlertType = AlertEventMappingSupport.normalize(event.alertType());
        String serviceName = AlertEventMappingSupport.defaultIfBlank(event.serviceName(), "서비스");

        return switch (normalizedAlertType) {
            case "APPLIED" -> AlertMessageTemplateRegistry.create(
                    NotificationType.SERVICE_ACCESS_BLOCKED,
                    serviceName
            );
            case "RELEASED" -> AlertMessageTemplateRegistry.create(
                    NotificationType.SERVICE_ACCESS_RELEASED,
                    serviceName
            );
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
        };
    }
}
