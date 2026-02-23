package hotspot.user.kafka.mapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.KafkaEventType;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.model.AlertNotificationContent;
import hotspot.user.kafka.model.AlertNotificationMappingResult;
import hotspot.user.notification.domain.Notification;

@Component
public class UserAlertEventNotificationMapper {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    // Kafka eventType에 따라 알림 매핑 메서드를 분기한다.
    public AlertNotificationMappingResult map(UserAlertEvent event) {
        KafkaEventType eventType = KafkaEventType.from(normalize(event.eventType()));
        return switch (eventType) {
            case USAGE_THRESHOLD -> mapUsageThreshold(event);
            case TIME_WINDOW_POLICY -> mapTimeWindowPolicy(event);
            case IMMEDIATE_BLOCK -> mapImmediateBlock(event);
            case SERVICE_ACCESS -> mapServiceAccess(event);
            case PRESENT_DATA -> mapPresentData(event);
        };
    }

    // 매핑 결과를 Notification 도메인 객체로 변환한다.
    public Notification toNotification(UserAlertEvent event) {
        AlertNotificationMappingResult mapping = map(event);
        return Notification.builder()
                .subId(requireSubId(event.subId()))
                .eventId(resolveEventId(event))
                .notificationType(mapping.notificationType().name())
                .content(mapping.content().body())
                .isRead(false)
                .createdTime(resolveCreatedTime(event))
                .build();
    }

    // 사용량 임계치 이벤트를 알림 타입/문구로 변환한다.
    private AlertNotificationMappingResult mapUsageThreshold(UserAlertEvent event) {
        String normalizedAlertType = normalize(event.alertType());
        int threshold = resolveThreshold(event);

        if (isSingleAlertType(normalizedAlertType)) {
            return switch (threshold) {
                case 50 -> create(NotificationType.SINGLE_USAGE_THRESHOLD_50, "데이터 임계치 알림", "데이터 잔여량이 50% 남았습니다.");
                case 30 -> create(NotificationType.SINGLE_USAGE_THRESHOLD_30, "데이터 임계치 알림", "데이터 잔여량이 30% 남았습니다.");
                case 10 -> create(NotificationType.SINGLE_USAGE_THRESHOLD_10, "데이터 임계치 알림", "데이터 잔여량이 10% 남았습니다.");
                case 0 -> create(NotificationType.SINGLE_USAGE_EXHAUSTED, "데이터 임계치 알림", "데이터 잔여량이 모두 소진되었습니다.");
                default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD);
            };
        }

        if (isFamilyAlertType(normalizedAlertType)) {
            return switch (threshold) {
                case 50 -> create(NotificationType.FAMILY_USAGE_THRESHOLD_50, "가족 공유 데이터 알림", "데이터 잔여량이 50% 남았습니다.");
                case 30 -> create(NotificationType.FAMILY_USAGE_THRESHOLD_30, "가족 공유 데이터 알림", "데이터 잔여량이 30% 남았습니다.");
                case 10 -> create(NotificationType.FAMILY_USAGE_THRESHOLD_10, "가족 공유 데이터 알림", "데이터 잔여량이 10% 남았습니다.");
                case 0 -> create(NotificationType.FAMILY_USAGE_EXHAUSTED, "가족 공유 데이터 알림", "데이터 잔여량이 모두 소진되었습니다.");
                default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD);
            };
        }

        if (isGiftAlertType(normalizedAlertType)) {
            return switch (threshold) {
                case 50 -> create(NotificationType.PRESENT_USAGE_THRESHOLD_50, "선물 데이터 알림", "데이터 잔여량이 50% 남았습니다.");
                case 30 -> create(NotificationType.PRESENT_USAGE_THRESHOLD_30, "선물 데이터 알림", "데이터 잔여량이 30% 남았습니다.");
                case 10 -> create(NotificationType.PRESENT_USAGE_THRESHOLD_10, "선물 데이터 알림", "데이터 잔여량이 10% 남았습니다.");
                case 0 -> create(NotificationType.PRESENT_USAGE_EXHAUSTED, "선물 데이터 알림", "데이터 잔여량이 모두 소진되었습니다.");
                default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD);
            };
        }

        throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
    }

    // 개인 요금제 잔여량 알림 타입인지 확인한다.
    private boolean isSingleAlertType(String alertType) {
        return "PLAN_REMAINING".equals(alertType);
    }

    // 가족 공유 풀 잔여량 알림 타입인지 확인한다.
    private boolean isFamilyAlertType(String alertType) {
        return "FAMILY_POOL_REMAINING".equals(alertType);
    }

    // 선물 데이터 잔여량 알림 타입인지 확인한다.
    private boolean isGiftAlertType(String alertType) {
        return "GIFT_REMAINING".equals(alertType);
    }

    // 시간 차단 정책 적용/해제 이벤트를 알림으로 변환한다.
    private AlertNotificationMappingResult mapTimeWindowPolicy(UserAlertEvent event) {
        String normalizedAlertType = normalize(event.alertType());
        String policyName = defaultIfBlank(event.policyName(), "정책");

        return switch (normalizedAlertType) {
            case "APPLIED" -> create(
                    NotificationType.TIME_WINDOW_POLICY_APPLIED,
                    "시간 차단 정책 알림",
                    "\"" + policyName + "\" 시간 차단 정책이 적용되었습니다"
            );
            case "RELEASED" -> create(
                    NotificationType.TIME_WINDOW_POLICY_RELEASED,
                    "시간 차단 정책 알림",
                    "\"" + policyName + "\" 시간 차단 정책이 해제되었습니다"
            );
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
        };
    }

    // 즉시 차단 적용/해제 이벤트를 알림으로 변환한다.
    private AlertNotificationMappingResult mapImmediateBlock(UserAlertEvent event) {
        String normalizedAlertType = normalize(event.alertType());

        return switch (normalizedAlertType) {
            case "APPLIED" -> create(
                    NotificationType.IMMEDIATE_BLOCK_APPLIED,
                    "즉시 차단 정책 알림",
                    "데이터 사용 차단이 즉시 적용되었습니다."
            );
            case "RELEASED" -> create(
                    NotificationType.IMMEDIATE_BLOCK_RELEASED,
                    "즉시 차단 정책 알림",
                    "데이터 사용 차단이 해제되었습니다."
            );
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
        };
    }

    // 서비스 차단 적용/해제 이벤트를 알림으로 변환한다.
    private AlertNotificationMappingResult mapServiceAccess(UserAlertEvent event) {
        String normalizedAlertType = normalize(event.alertType());
        String serviceName = defaultIfBlank(event.serviceName(), "서비스");

        return switch (normalizedAlertType) {
            case "APPLIED", "BLOCKED" -> create(
                    NotificationType.SERVICE_ACCESS_BLOCKED,
                    "앱/서비스 이용 차단 알림",
                    "\"" + serviceName + "\" 서비스 이용이 차단되었습니다."
            );
            case "RELEASED", "UNBLOCKED" -> create(
                    NotificationType.SERVICE_ACCESS_RELEASED,
                    "앱/서비스 이용 차단 알림",
                    "\"" + serviceName + "\" 서비스 이용 차단이 해제되었습니다."
            );
            default -> throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_ALERT_TYPE);
        };
    }

    // 데이터 선물 이벤트를 알림으로 변환한다.
    private AlertNotificationMappingResult mapPresentData(UserAlertEvent event) {
        String senderName = defaultIfBlank(event.presentSenderName(), "사용자");
        String presentAmount = defaultIfBlank(event.presentAmount(), "0MB");

        return create(
                NotificationType.PRESENT_DATA,
                "데이터 선물 알림",
                "\"" + senderName + "\"님이 데이터 " + presentAmount + "을 선물하셨습니다"
        );
    }

    // 공통 매핑 결과 객체를 생성한다.
    private AlertNotificationMappingResult create(NotificationType notificationType, String title, String body) {
        return new AlertNotificationMappingResult(
                notificationType,
                new AlertNotificationContent(title, body)
        );
    }

    // threshold 문자열 또는 remainingPct에서 임계치 값을 추출한다.
    private int resolveThreshold(UserAlertEvent event) {
        String thresholdRaw = defaultIfBlank(event.threshold(), String.valueOf(event.remainingPct()));
        Matcher matcher = NUMBER_PATTERN.matcher(thresholdRaw);
        if (!matcher.find()) {
            throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD);
        }
        return Integer.parseInt(matcher.group());
    }

    // sourceEventId 우선으로 알림 이벤트 ID를 결정한다.
    private String resolveEventId(UserAlertEvent event) {
        String eventId = defaultIfBlank(event.sourceEventId(), event.alertId());
        if (eventId.isBlank()) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_EVENT_ID_REQUIRED);
        }
        return eventId;
    }

    // occurredAt을 UTC 기준 LocalDateTime으로 변환한다.
    private LocalDateTime resolveCreatedTime(UserAlertEvent event) {
        if (event.occurredAt() == null) {
            return LocalDateTime.now(ZoneOffset.UTC);
        }
        return LocalDateTime.ofInstant(event.occurredAt(), ZoneOffset.UTC);
    }

    // 대소문자/구분자 차이를 제거해 비교 가능한 문자열로 정규화한다.
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
    }

    // 문자열이 비어있으면 기본값을 반환한다.
    private static String defaultIfBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    // subId 필수값을 검증한다.
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
