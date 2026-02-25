package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OutboxErrorCode implements BaseErrorCode {
    OUTBOX_PAYLOAD_SERIALIZATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "OUTBOX_001",
            "Outbox payload의 직렬화를 실패하였습니다."
    ),
    OUTBOX_EVENT_SAVE_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "OUTBOX_002",
            "Outbox event 저장을 실패하였습니다."
    ),
    OUTBOX_EVENT_PUBLISH_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "OUTBOX_003",
            "Outbox event 발행을 실패하였습니다."
    ),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
