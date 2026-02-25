package hotspot.user.kafka.mapper.strategy;

import hotspot.user.kafka.domain.KafkaEventType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.model.AlertNotificationMappingResult;

public interface UserAlertEventMappingStrategy {

    boolean supports(KafkaEventType eventType);

    AlertNotificationMappingResult map(UserAlertEvent event);
}
