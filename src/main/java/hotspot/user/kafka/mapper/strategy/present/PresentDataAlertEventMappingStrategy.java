package hotspot.user.kafka.mapper.strategy.present;

import org.springframework.stereotype.Component;

import hotspot.user.kafka.domain.KafkaEventType;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.mapper.strategy.UserAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.support.AlertEventMappingSupport;
import hotspot.user.kafka.mapper.template.AlertMessageTemplateRegistry;
import hotspot.user.kafka.model.AlertNotificationMappingResult;

@Component
public class PresentDataAlertEventMappingStrategy implements UserAlertEventMappingStrategy {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return eventType == KafkaEventType.PRESENT_DATA;
    }

    @Override
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        String senderName = AlertEventMappingSupport.defaultIfBlank(event.presentSenderName(), "사용자");
        String presentAmount = AlertEventMappingSupport.defaultIfBlank(event.presentAmount(), "0MB");

        return AlertMessageTemplateRegistry.create(
                NotificationType.PRESENT_DATA,
                senderName,
                presentAmount
        );
    }
}
