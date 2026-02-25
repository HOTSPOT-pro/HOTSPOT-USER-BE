package hotspot.user.kafka.mapper.strategy.appservice;

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
public class AppServiceAlertEventMappingStrategy implements UserAlertEventMappingStrategy {

    // 이 전략이 SERVICE_ACCESS 이벤트 타입을 처리하는지 여부를 반환한다.
    @Override
    public boolean supports(KafkaEventType eventType) {
        return eventType == KafkaEventType.SERVICE_ACCESS;
    }

    // 알림 타입과 서비스명을 정규화한 뒤, 차단/해제 케이스에 맞는 알림 내용을 생성해 반환한다.
    @Override
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        String normalizedAlertType = AlertEventMappingSupport.normalize(event.alertType());
        String serviceName = AlertEventMappingSupport.defaultIfBlank(event.serviceName(), "서비스");

        return switch (normalizedAlertType) {
            case "APPLIED" -> AlertEventMappingSupport.create(
                    NotificationType.SERVICE_ACCESS_BLOCKED,
                    "앱 서비스 이용 차단 알림",
                    "\"" + serviceName + "\" 서비스 이용이 차단되었습니다."
            );
            case "RELEASED" -> AlertEventMappingSupport.create(
                    NotificationType.SERVICE_ACCESS_RELEASED,
                    "앱 서비스 이용 차단 알림",
                    "\"" + serviceName + "\" 서비스 이용 차단이 해제되었습니다."
            );
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
        };
    }
}
