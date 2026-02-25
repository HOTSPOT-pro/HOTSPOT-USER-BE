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
            "Outbox payload serialization failed."
    ),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
