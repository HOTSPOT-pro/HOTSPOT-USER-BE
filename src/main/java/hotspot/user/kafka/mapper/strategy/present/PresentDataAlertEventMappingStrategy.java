package hotspot.user.kafka.mapper.strategy.present;

import org.springframework.stereotype.Component;

import hotspot.user.kafka.domain.KafkaEventType;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.mapper.strategy.UserAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.support.AlertEventMappingSupport;
import hotspot.user.kafka.model.AlertNotificationMappingResult;

@Component
public class PresentDataAlertEventMappingStrategy implements UserAlertEventMappingStrategy {

    // 이 전략이 PRESENT_DATA 이벤트 타입을 처리하는지 여부를 반환한다.
    @Override
    public boolean supports(KafkaEventType eventType) {
        return eventType == KafkaEventType.PRESENT_DATA;
    }

    // 발신자 이름과 선물 데이터 용량을 기본값 처리한 뒤, 데이터 선물 알림 내용을 생성해 반환한다.
    @Override
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        String senderName = AlertEventMappingSupport.defaultIfBlank(event.presentSenderName(), "사용자");
        String presentAmount = AlertEventMappingSupport.defaultIfBlank(event.presentAmount(), "0MB");

        return AlertEventMappingSupport.create(
                NotificationType.PRESENT_DATA,
                "데이터 선물 알림",
                "\"" + senderName + "\"님이 데이터 " + presentAmount + "를 선물해줬습니다."
        );
    }
}
