package hotspot.user.kafka.mapper.strategy.usage;

import java.util.List;
import java.util.Map;

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
import hotspot.user.presentData.service.port.PresentDataRepository;

@Component
public class UsageThresholdAlertEventMappingStrategy implements UserAlertEventMappingStrategy {

    private final PresentDataRepository presentDataRepository;

    public UsageThresholdAlertEventMappingStrategy(PresentDataRepository presentDataRepository) {
        this.presentDataRepository = presentDataRepository;
    }

    @Override
    public boolean supports(KafkaEventType eventType) {
        return eventType == KafkaEventType.USAGE_THRESHOLD;
    }

    @Override
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        String normalizedAlertType = AlertEventMappingSupport.normalize(event.alertType());
        int threshold = AlertEventMappingSupport.resolveThreshold(event);

        if (isSingleAlertType(normalizedAlertType)) {
            return AlertMessageTemplateRegistry.create(resolveSingleNotificationType(threshold));
        }

        if (isFamilyAlertType(normalizedAlertType)) {
            return AlertMessageTemplateRegistry.create(resolveFamilyNotificationType(threshold));
        }

        if (isGiftAlertType(normalizedAlertType)) {
            String senderName = resolveGiftSenderName(event);
            return AlertMessageTemplateRegistry.create(resolveGiftNotificationType(threshold), senderName);
        }

        throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
    }

    private NotificationType resolveSingleNotificationType(int threshold) {
        return switch (threshold) {
            case 50 -> NotificationType.SINGLE_USAGE_THRESHOLD_50;
            case 30 -> NotificationType.SINGLE_USAGE_THRESHOLD_30;
            case 10 -> NotificationType.SINGLE_USAGE_THRESHOLD_10;
            case 0 -> NotificationType.SINGLE_USAGE_EXHAUSTED;
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD);
        };
    }

    private NotificationType resolveFamilyNotificationType(int threshold) {
        return switch (threshold) {
            case 50 -> NotificationType.FAMILY_USAGE_THRESHOLD_50;
            case 30 -> NotificationType.FAMILY_USAGE_THRESHOLD_30;
            case 10 -> NotificationType.FAMILY_USAGE_THRESHOLD_10;
            case 0 -> NotificationType.FAMILY_USAGE_EXHAUSTED;
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD);
        };
    }

    private NotificationType resolveGiftNotificationType(int threshold) {
        return switch (threshold) {
            case 50 -> NotificationType.PRESENT_USAGE_THRESHOLD_50;
            case 30 -> NotificationType.PRESENT_USAGE_THRESHOLD_30;
            case 10 -> NotificationType.PRESENT_USAGE_THRESHOLD_10;
            case 0 -> NotificationType.PRESENT_USAGE_EXHAUSTED;
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD);
        };
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

    private String resolveGiftSenderName(UserAlertEvent event) {
        String senderNameFromEvent = AlertEventMappingSupport.defaultIfBlank(event.presentSenderName(), null);
        if (senderNameFromEvent != null) {
            return senderNameFromEvent;
        }

        Long giftId = parseGiftId(event.giftId());
        if (giftId != null) {
            Map<Long, String> giverNames = presentDataRepository.findGiftGiverNames(List.of(giftId));
            String senderNameFromGift = giverNames.get(giftId);
            if (senderNameFromGift != null && !senderNameFromGift.isBlank()) {
                return senderNameFromGift;
            }
        }

        return "누군가";
    }

    private Long parseGiftId(String giftIdRaw) {
        if (giftIdRaw == null || giftIdRaw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(giftIdRaw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
