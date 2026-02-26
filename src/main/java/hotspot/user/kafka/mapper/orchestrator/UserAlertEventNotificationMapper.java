package hotspot.user.kafka.mapper.orchestrator;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.KafkaEventType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.mapper.strategy.UserAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.support.AlertEventMappingSupport;
import hotspot.user.kafka.model.AlertNotificationMappingResult;
import hotspot.user.notification.domain.Notification;

@Component
public class UserAlertEventNotificationMapper {

    private final Map<KafkaEventType, UserAlertEventMappingStrategy> strategiesByEventType;

    public UserAlertEventNotificationMapper(List<UserAlertEventMappingStrategy> mappingStrategies) {
        this.strategiesByEventType = new EnumMap<>(KafkaEventType.class);
        for (KafkaEventType eventType : KafkaEventType.values()) {
            UserAlertEventMappingStrategy strategy = resolveSingleStrategy(eventType, mappingStrategies);
            this.strategiesByEventType.put(eventType, strategy);
        }
    }

    public AlertNotificationMappingResult map(UserAlertEvent event) {
        KafkaEventType eventType = KafkaEventType.from(AlertEventMappingSupport.normalize(event.eventType()));
        UserAlertEventMappingStrategy strategy = strategiesByEventType.get(eventType);
        return strategy.map(event);
    }

    public Notification toNotification(UserAlertEvent event) {
        return toNotification(event, event.subId());
    }

    public Notification toNotification(UserAlertEvent event, Long targetSubId) {
        AlertNotificationMappingResult mapping = map(event);
        return Notification.builder()
                .subId(requireSubId(targetSubId))
                .eventId(requireEventId(event.alertId()))
                .notificationType(mapping.notificationType().name())
                .title(mapping.content().title())
                .content(mapping.content().body())
                .isRead(false)
                .createdTime(resolveCreatedTime(event))
                .build();
    }

    private String requireEventId(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_EVENT_ID_REQUIRED);
        }
        return eventId;
    }

    private LocalDateTime resolveCreatedTime(UserAlertEvent event) {
        if (event.createdTime() == null) {
            return LocalDateTime.now(ZoneOffset.UTC);
        }
        return event.createdTime();
    }

    private static Long requireSubId(Long subId) {
        if (subId == null) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_SUB_ID_REQUIRED);
        }
        if (subId <= 0L) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_SUB_ID_INVALID);
        }
        return subId;
    }

    private UserAlertEventMappingStrategy resolveSingleStrategy(
            KafkaEventType eventType,
            List<UserAlertEventMappingStrategy> mappingStrategies
    ) {
        List<UserAlertEventMappingStrategy> matchedStrategies = mappingStrategies.stream()
                .filter(strategy -> strategy.supports(eventType))
                .toList();

        if (matchedStrategies.isEmpty()) {
            throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        }
        if (matchedStrategies.size() > 1) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_MAPPING_STRATEGY_DUPLICATED);
        }
        return matchedStrategies.get(0);
    }
}
