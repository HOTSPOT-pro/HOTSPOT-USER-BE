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
import hotspot.user.kafka.model.AlertNotificationMappingResult;

@Component
public class FamilyMemberApplyAlertEventMappingStrategy implements UserAlertEventMappingStrategy {

    // 가족 구성원 추가/삭제(FAMILY_MEMBER_ADD/REMOVE) 이벤트를 처리할지 여부를 판단한다.
    @Override
    public boolean supports(KafkaEventType eventType) {
        return eventType == KafkaEventType.FAMILY_CREATE
                || eventType == KafkaEventType.FAMILY_MEMBER_ADD
                || eventType == KafkaEventType.FAMILY_MEMBER_REMOVE;
    }

    // 이벤트 타입과 승인/반려 타입을 정규화해 알림 타입·제목·본문을 결정하고 매핑 결과를 생성한다.
    @Override
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        KafkaEventType eventType = KafkaEventType.from(AlertEventMappingSupport.normalize(event.eventType()));
        String alertType = AlertEventMappingSupport.normalize(event.alertType());
        String targetName = resolveTargetNames(event.targetNames());

        return switch (alertType) {
            case "APPROVED" -> AlertEventMappingSupport.create(
                    approvedType(eventType),
                    approvedTitle(eventType),
                    "\"" + targetName + "\" " + approvedBody(eventType)
            );
            case "REJECTED" -> AlertEventMappingSupport.create(
                    rejectedType(eventType),
                    rejectedTitle(eventType),
                    "\"" + targetName + "\" " + rejectedBody(eventType)
            );
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
        };
    }

    private String resolveTargetNames(List<String> targetNames) {
        if (targetNames == null || targetNames.isEmpty()) {
            return "member";
        }

        String names = targetNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));

        return AlertEventMappingSupport.defaultIfBlank(names, "member");
    }

    // 이벤트 타입에 맞는 승인 알림 NotificationType을 반환한다.
    private NotificationType approvedType(KafkaEventType eventType) {
        return switch (eventType) {
            case FAMILY_CREATE -> NotificationType.FAMILY_CREATE_APPROVED;
            case FAMILY_MEMBER_ADD -> NotificationType.FAMILY_MEMBER_ADD_APPROVED;
            case FAMILY_MEMBER_REMOVE -> NotificationType.FAMILY_MEMBER_REMOVE_APPROVED;
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        };
    }

    // 이벤트 타입에 맞는 반려 알림 NotificationType을 반환한다.
    private NotificationType rejectedType(KafkaEventType eventType) {
        return switch (eventType) {
            case FAMILY_CREATE -> NotificationType.FAMILY_CREATE_REJECTED;
            case FAMILY_MEMBER_ADD -> NotificationType.FAMILY_MEMBER_ADD_REJECTED;
            case FAMILY_MEMBER_REMOVE -> NotificationType.FAMILY_MEMBER_REMOVE_REJECTED;
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        };
    }

    // 이벤트 타입에 맞는 승인 알림 제목을 반환한다.
    private String approvedTitle(KafkaEventType eventType) {
        return switch (eventType) {
            case FAMILY_CREATE -> "신규 가족 생성 승인";
            case FAMILY_MEMBER_ADD -> "가족 구성원 추가 승인";
            case FAMILY_MEMBER_REMOVE -> "가족 구성원 삭제 승인";
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        };
    }

    // 이벤트 타입에 맞는 반려 알림 제목을 반환한다.
    private String rejectedTitle(KafkaEventType eventType) {
        return switch (eventType) {
            case FAMILY_CREATE -> "신규 가족 생성 반려";
            case FAMILY_MEMBER_ADD -> "가족 구성원 추가 반려";
            case FAMILY_MEMBER_REMOVE -> "가족 구성원 삭제 반려";
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        };
    }

    // 이벤트 타입에 맞는 승인 알림 본문 문구를 반환한다.
    private String approvedBody(KafkaEventType eventType) {
        return switch (eventType) {
            case FAMILY_CREATE -> "님의 신규 가족 생성이 승인되었습니다.";
            case FAMILY_MEMBER_ADD -> "님의 구성원 추가가 승인되었습니다.";
            case FAMILY_MEMBER_REMOVE -> "님의 구성원 삭제가 승인되었습니다.";
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        };
    }

    // 이벤트 타입에 맞는 반려 알림 본문 문구를 반환한다.
    private String rejectedBody(KafkaEventType eventType) {
        return switch (eventType) {
            case FAMILY_CREATE -> "님의 신규 가족 생성이 반려되었습니다.";
            case FAMILY_MEMBER_ADD -> "님의 구성원 추가가 반려되었습니다.";
            case FAMILY_MEMBER_REMOVE -> "님의 구성원 삭제가 반려되었습니다.";
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        };
    }
}
