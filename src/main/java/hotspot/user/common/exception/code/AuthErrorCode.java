package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    REFRESH_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH_001", "리프레시 토큰을 찾을 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
    TOKEN_NOT_MATCH(HttpStatus.UNAUTHORIZED, "AUTH_003", "토큰 정보가 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
