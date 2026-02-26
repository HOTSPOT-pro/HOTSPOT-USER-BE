package hotspot.user.kafka.mapper.strategy.usage;

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
public class UsageThresholdAlertEventMappingStrategy implements UserAlertEventMappingStrategy {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return eventType == KafkaEventType.USAGE_THRESHOLD;
    }

    @Override
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        String normalizedAlertType = AlertEventMappingSupport.normalize(event.alertType());
        int threshold = AlertEventMappingSupport.resolveThreshold(event);

        if (isSingleAlertType(normalizedAlertType)) {
            return switch (threshold) {
                case 50 -> AlertEventMappingSupport.create(
                        NotificationType.SINGLE_USAGE_THRESHOLD_50,
                        "개인 요금제 데이터 알림",
                        "데이터 잔여량이 50% 이하입니다."
                );
                case 30 -> AlertEventMappingSupport.create(
                        NotificationType.SINGLE_USAGE_THRESHOLD_30,
                        "개인 요금제 데이터 알림",
                        "데이터 잔여량이 30% 이하입니다."
                );
                case 10 -> AlertEventMappingSupport.create(
                        NotificationType.SINGLE_USAGE_THRESHOLD_10,
                        "개인 요금제 데이터 알림",
                        "데이터 잔여량이 10% 이하입니다."
                );
                case 0 -> AlertEventMappingSupport.create(
                        NotificationType.SINGLE_USAGE_EXHAUSTED,
                        "개인 요금제 데이터 알림",
                        "데이터 잔여량이 모두 소진되었습니다."
                );
                default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD);
            };
        }

        if (isFamilyAlertType(normalizedAlertType)) {
            return switch (threshold) {
                case 50 -> AlertEventMappingSupport.create(
                        NotificationType.FAMILY_USAGE_THRESHOLD_50,
                        "가족 공유 데이터 알림",
                        "데이터 잔여량이 50% 이하입니다."
                );
                case 30 -> AlertEventMappingSupport.create(
                        NotificationType.FAMILY_USAGE_THRESHOLD_30,
                        "가족 공유 데이터 알림",
                        "데이터 잔여량이 30% 이하입니다."
                );
                case 10 -> AlertEventMappingSupport.create(
                        NotificationType.FAMILY_USAGE_THRESHOLD_10,
                        "가족 공유 데이터 알림",
                        "데이터 잔여량이 10% 이하입니다."
                );
                case 0 -> AlertEventMappingSupport.create(
                        NotificationType.FAMILY_USAGE_EXHAUSTED,
                        "가족 공유 데이터 알림",
                        "데이터 잔여량이 모두 소진되었습니다."
                );
                default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD);
            };
        }

        if (isGiftAlertType(normalizedAlertType)) {
            return switch (threshold) {
                case 50 -> AlertEventMappingSupport.create(
                        NotificationType.PRESENT_USAGE_THRESHOLD_50,
                        "선물 데이터 알림",
                        "데이터 잔여량이 50% 이하입니다."
                );
                case 30 -> AlertEventMappingSupport.create(
                        NotificationType.PRESENT_USAGE_THRESHOLD_30,
                        "선물 데이터 알림",
                        "데이터 잔여량이 30% 이하입니다."
                );
                case 10 -> AlertEventMappingSupport.create(
                        NotificationType.PRESENT_USAGE_THRESHOLD_10,
                        "선물 데이터 알림",
                        "데이터 잔여량이 10% 이하입니다."
                );
                case 0 -> AlertEventMappingSupport.create(
                        NotificationType.PRESENT_USAGE_EXHAUSTED,
                        "선물 데이터 알림",
                        "데이터 잔여량이 모두 소진되었습니다."
                );
                default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD);
            };
        }

        throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
    }

    private boolean isSingleAlertType(String alertType) {
        return "PLAN_REMAINING".equals(alertType);
    }

    private boolean isFamilyAlertType(String alertType) {
        return "FAMILY_POOL_REMAINING".equals(alertType);
    }

    private boolean isGiftAlertType(String alertType) {
        return "GIFT_REMAINING".equals(alertType);
    }
}
