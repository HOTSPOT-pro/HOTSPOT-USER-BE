package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회선 도메인 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum PolicyErrorCode implements BaseErrorCode {
    POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "POLICY_001", "해당 정책을 찾을 수 없습니다."),
    POLICY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "POLICY_002", "관리자 또는 우리 가족이 직접 만든 정책만 사용할 수 있습니다."),
    INACTIVE_POLICY_CANNOT_APPLY(HttpStatus.BAD_REQUEST, "POLICY_003", "현재 비활성화된 정책은 적용할 수 없습니다."),
    INVALID_POLICY_FORMAT(HttpStatus.BAD_REQUEST, "POLICY_004", "정책 타입에 유효하지 않은 스냅샷 형식입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
