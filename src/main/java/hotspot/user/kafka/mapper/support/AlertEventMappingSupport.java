package hotspot.user.kafka.mapper.support;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.model.AlertNotificationContent;
import hotspot.user.kafka.model.AlertNotificationMappingResult;

public final class AlertEventMappingSupport {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    private AlertEventMappingSupport() {
    }

    // 알림 타입과 제목/본문으로 AlertNotificationMappingResult를 생성해 반환한다.
    public static AlertNotificationMappingResult create(
            NotificationType notificationType,
            String title,
            String body
    ) {
        return new AlertNotificationMappingResult(
                notificationType,
                new AlertNotificationContent(title, body)
        );
    }

    // 문자열을 trim 후 -과 공백을 _로 바꾸고, 대문자로 통일해 표준 키 형태로 만든다.
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
    }

    // 값이 null이거나 공백이면 대체값을 반환한다.
    public static String defaultIfBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    // 이벤트의 임계치 문자열을 가져와 숫자만 추출해 임계치로 반환한다.
    public static int resolveThreshold(UserAlertEvent event) {
        String thresholdRaw = defaultIfBlank(event.threshold(), String.valueOf(event.remainingPct()));
        Matcher matcher = NUMBER_PATTERN.matcher(thresholdRaw);
        if (!matcher.find()) {
            throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_THRESHOLD);
        }
        return Integer.parseInt(matcher.group());
    }
}
