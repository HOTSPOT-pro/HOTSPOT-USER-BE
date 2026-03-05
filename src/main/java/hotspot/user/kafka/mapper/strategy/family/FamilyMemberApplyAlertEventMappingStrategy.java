package hotspot.user.kafka.mapper.strategy.family;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

@Component
public class FamilyMemberApplyAlertEventMappingStrategy implements UserAlertEventMappingStrategy {

    @Override
    public boolean supports(KafkaEventType eventType) {
        return eventType == KafkaEventType.FAMILY_CREATE
                || eventType == KafkaEventType.FAMILY_MEMBER_ADD
                || eventType == KafkaEventType.FAMILY_MEMBER_REMOVE;
    }

    @Override
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        KafkaEventType eventType = KafkaEventType.from(AlertEventMappingSupport.normalize(event.eventType()));
        String alertType = AlertEventMappingSupport.normalize(event.alertType());
        String targetName = resolveTargetNames(event.targetNames());

        NotificationType notificationType = resolveNotificationType(eventType, alertType);
        return AlertMessageTemplateRegistry.create(notificationType, targetName);
    }

    private NotificationType resolveNotificationType(KafkaEventType eventType, String alertType) {
        return switch (alertType) {
            case "APPROVED" -> approvedType(eventType);
            case "REJECTED" -> rejectedType(eventType);
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
        };
    }

    private String resolveTargetNames(List<String> targetNames) {
        if (targetNames == null || targetNames.isEmpty()) {
            return "멤버";
        }

        String names = targetNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));

        return AlertEventMappingSupport.defaultIfBlank(names, "멤버");
    }

    private NotificationType approvedType(KafkaEventType eventType) {
        return switch (eventType) {
            case FAMILY_CREATE -> NotificationType.FAMILY_CREATE_APPROVED;
            case FAMILY_MEMBER_ADD -> NotificationType.FAMILY_MEMBER_ADD_APPROVED;
            case FAMILY_MEMBER_REMOVE -> NotificationType.FAMILY_MEMBER_REMOVE_APPROVED;
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        };
    }

    private NotificationType rejectedType(KafkaEventType eventType) {
        return switch (eventType) {
            case FAMILY_CREATE -> NotificationType.FAMILY_CREATE_REJECTED;
            case FAMILY_MEMBER_ADD -> NotificationType.FAMILY_MEMBER_ADD_REJECTED;
            case FAMILY_MEMBER_REMOVE -> NotificationType.FAMILY_MEMBER_REMOVE_REJECTED;
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        };
    }
}
