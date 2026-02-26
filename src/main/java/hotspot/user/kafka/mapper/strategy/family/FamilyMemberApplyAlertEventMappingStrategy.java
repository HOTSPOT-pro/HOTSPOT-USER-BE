package hotspot.user.kafka.mapper.strategy.family;

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
public class FamilyMemberApplyAlertEventMappingStrategy implements UserAlertEventMappingStrategy {

    // 들어온 Kafka 이벤트 타입이 FAMILY_MEMBER_ADD일 때만 이 매핑 전략을 적용한다.
    @Override
    public boolean supports(KafkaEventType eventType) {
        return eventType == KafkaEventType.FAMILY_MEMBER_ADD;
    }

    // 승인/반려에 맞는 NotificationType·제목·내용으로 알림 매핑 결과를 만들고 그 외 타입은 예외를 던진다.
    @Override
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        String alertType = AlertEventMappingSupport.normalize(event.alertType());
        String targetName = AlertEventMappingSupport.defaultIfBlank(event.targetName(), "구성원");

        return switch (alertType) {
            case "APPROVED" -> AlertEventMappingSupport.create(
                    NotificationType.FAMILY_MEMBER_ADD_APPROVED,
                    "가족 구성원 추가 승인",
                    "\"" + targetName + "\" 구성원 추가가 승인되었습니다."
            );
            case "REJECTED" -> AlertEventMappingSupport.create(
                    NotificationType.FAMILY_MEMBER_ADD_REJECTED,
                    "가족 구성원 추가 반려",
                    "\"" + targetName + "\" 구성원 추가가 반려되었습니다."
            );
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
        };
    }
}
