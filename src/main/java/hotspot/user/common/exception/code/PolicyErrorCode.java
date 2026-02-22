package hotspot.user.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 회선 도메인 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum PolicyErrorCode implements BaseErrorCode {
    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "POLICY_001", "해당 정책을 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
