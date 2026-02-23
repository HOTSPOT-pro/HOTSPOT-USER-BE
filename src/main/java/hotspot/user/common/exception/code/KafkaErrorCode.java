package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaErrorCode implements BaseErrorCode {
    UNSUPPORTED_KAFKA_EVENT_TYPE(HttpStatus.BAD_REQUEST, "KAFKA_001", "吏?먰븯吏 ?딅뒗 Kafka eventType ?낅땲??"),
    UNSUPPORTED_KAFKA_ALERT_TYPE(HttpStatus.BAD_REQUEST, "KAFKA_002", "吏?먰븯吏 ?딅뒗 Kafka alertType ?낅땲??"),
    UNSUPPORTED_KAFKA_THRESHOLD(HttpStatus.BAD_REQUEST, "KAFKA_003", "吏?먰븯吏 ?딅뒗 Kafka threshold ?낅땲??"),
    KAFKA_EVENT_ID_REQUIRED(HttpStatus.BAD_REQUEST, "KAFKA_004", "Kafka eventId(sourceEventId or alertId)媛 ?꾩슂?⑸땲??"),
    KAFKA_SUB_ID_REQUIRED(HttpStatus.BAD_REQUEST, "KAFKA_005", "Kafka subId媛 ?꾩슂?⑸땲??"),
    KAFKA_SUB_ID_INVALID(HttpStatus.BAD_REQUEST, "KAFKA_006", "Kafka subId媛 ?좏슚?섏? ?딆뒿?덈떎."),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
