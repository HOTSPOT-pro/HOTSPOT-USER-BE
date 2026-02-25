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

    // 등록된 매핑 전략을 이벤트 타입별 1:1 맵으로 초기화한다.
    public UserAlertEventNotificationMapper(List<UserAlertEventMappingStrategy> mappingStrategies) {
        this.strategiesByEventType = new EnumMap<>(KafkaEventType.class);
        for (KafkaEventType eventType : KafkaEventType.values()) {
            UserAlertEventMappingStrategy strategy = resolveSingleStrategy(eventType, mappingStrategies);
            this.strategiesByEventType.put(eventType, strategy);
        }
    }

    // 이벤트 타입을 해석한 뒤 해당 전략으로 매핑 결과를 생성한다.
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        KafkaEventType eventType = KafkaEventType.from(AlertEventMappingSupport.normalize(event.eventType()));
        UserAlertEventMappingStrategy strategy = strategiesByEventType.get(eventType);
        return strategy.map(event);
    }

    // 이벤트의 subId를 대상으로 Notification 엔티티를 생성한다.
    public Notification toNotification(UserAlertEvent event) {
        return toNotification(event, event.subId());
    }

    // 지정된 targetSubId를 대상으로 Notification 엔티티를 생성한다.
    public Notification toNotification(UserAlertEvent event, Long targetSubId) {
        AlertNotificationMappingResult mapping = map(event);
        return Notification.builder()
                .subId(requireSubId(targetSubId))
                .eventId(resolveEventId(event))
                .notificationType(mapping.notificationType().name())
                .title(mapping.content().title())
                .content(mapping.content().body())
                .isRead(false)
                .createdTime(resolveCreatedTime(event))
                .build();
    }

    // sourceEventId를 우선 사용하고 비어 있으면 alertId로 대체한다.
    private String resolveEventId(UserAlertEvent event) {
        String eventId = AlertEventMappingSupport.defaultIfBlank(event.sourceEventId(), event.alertId());
        if (eventId.isBlank()) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_EVENT_ID_REQUIRED);
        }
        return eventId;
    }

    // occurredAt을 UTC 기준 LocalDateTime으로 변환한다. 값이 없으면 현재 UTC 시간을 사용한다.
    private LocalDateTime resolveCreatedTime(UserAlertEvent event) {
        if (event.occurredAt() == null) {
            return LocalDateTime.now(ZoneOffset.UTC);
        }
        return LocalDateTime.ofInstant(event.occurredAt(), ZoneOffset.UTC);
    }

    // subId의 null/양수 여부를 검증한다.
    private static Long requireSubId(Long subId) {
        if (subId == null) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_SUB_ID_REQUIRED);
        }
        if (subId <= 0L) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_SUB_ID_INVALID);
        }
        return subId;
    }

    // 이벤트 타입별 전략이 정확히 1개인지 검증하고 반환한다.
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
