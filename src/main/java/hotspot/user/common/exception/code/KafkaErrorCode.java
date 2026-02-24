package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaErrorCode implements BaseErrorCode {
    UNSUPPORTED_KAFKA_EVENT_TYPE(HttpStatus.BAD_REQUEST, "KAFKA_001", "지원하지 않는 Kafka eventType 입니다."),
    UNSUPPORTED_KAFKA_ALERT_TYPE(HttpStatus.BAD_REQUEST, "KAFKA_002", "지원하지 않는 Kafka alertType 입니다."),
    UNSUPPORTED_KAFKA_THRESHOLD(HttpStatus.BAD_REQUEST, "KAFKA_003", "지원하지 않는 Kafka threshold 입니다."),
    KAFKA_EVENT_ID_REQUIRED(HttpStatus.BAD_REQUEST, "KAFKA_004", "Kafka eventId가 필요합니다."),
    KAFKA_SUB_ID_REQUIRED(HttpStatus.BAD_REQUEST, "KAFKA_005", "Kafka subId가 필요합니다."),
    KAFKA_SUB_ID_INVALID(HttpStatus.BAD_REQUEST, "KAFKA_006", "Kafka subId가 유효하지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
