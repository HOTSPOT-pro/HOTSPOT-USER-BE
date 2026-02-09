package hotspot.user.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExErrorCode implements BaseErrorCode {

    EX_NOT_FOUND(HttpStatus.NOT_FOUND, "EX_001", "EX를 찾을 수 없습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
