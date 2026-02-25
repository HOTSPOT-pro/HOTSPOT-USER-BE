package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum S3ErrorCode implements BaseErrorCode {

    EXTENSION_NOT_PNG(HttpStatus.BAD_REQUEST, "S3_001", "파일 확장자로 PNG만 가능합니다"),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
