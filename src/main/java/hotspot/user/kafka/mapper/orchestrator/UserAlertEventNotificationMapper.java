package hotspot.user.kafka.mapper.orchestrator;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.KafkaEventType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.mapper.registry.UserAlertEventMappingStrategyRegistry;
import hotspot.user.kafka.mapper.support.AlertEventMappingSupport;
import hotspot.user.kafka.model.AlertNotificationMappingResult;
import hotspot.user.notification.domain.Notification;

@Component
public class UserAlertEventNotificationMapper {

    private final UserAlertEventMappingStrategyRegistry strategyRegistry;

    public UserAlertEventNotificationMapper(UserAlertEventMappingStrategyRegistry strategyRegistry) {
        this.strategyRegistry = strategyRegistry;
    }

    // 이벤트 타입을 정규화해 매핑 전략을 선택하고 알림 타입/문구 매핑 결과를 생성한다.
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        KafkaEventType eventType = KafkaEventType.from(AlertEventMappingSupport.normalize(event.eventType()));
        return strategyRegistry.resolve(eventType).map(event);
    }

    // 이벤트에 포함된 subId를 대상으로 Notification 객체를 생성한다.
    public Notification toNotification(UserAlertEvent event) {
        return toNotification(event, event.subId());
    }

    // 지정된 대상 subId로 Notification을 생성하며 eventId/타입/문구/시간을 매핑해 채운다.
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

    // eventId가 비어있으면 예외를 발생시키고 유효한 eventId만 반환한다.
    private String requireEventId(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_EVENT_ID_REQUIRED);
        }
        return eventId;
    }

    // 이벤트 생성 시간이 없으면 현재 UTC 시간을 사용하고 있으면 그 값을 사용한다.
    private LocalDateTime resolveCreatedTime(UserAlertEvent event) {
        if (event.createdTime() == null) {
            return LocalDateTime.now(ZoneOffset.UTC);
        }
        return event.createdTime();
    }

    // subId가 null이거나 0 이하이면 예외를 발생시키고 유효한 subId만 반환한다.
    private static Long requireSubId(Long subId) {
        if (subId == null) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_SUB_ID_REQUIRED);
        }
        if (subId <= 0L) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_SUB_ID_INVALID);
        }
        return subId;
    }
}
