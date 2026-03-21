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
    DUPLICATE_DAY_OF_WEEK(HttpStatus.BAD_REQUEST, "POLICY_005", "중복된 요일이 포함되어 있습니다."),
    ALREADY_DELETED_POLICY(HttpStatus.BAD_REQUEST, "POLICY_006", "이미 삭제된 정책은 수정할 수 없습니다."),
    INVALID_TIME_FORMAT(HttpStatus.BAD_REQUEST, "POLICY_007", "유효하지 않은 시간 형식입니다. (HH:mm)"),
    UNNECESSARY_SNAPSHOT_FIELD(HttpStatus.BAD_REQUEST, "POLICY_008", "해당 정책 타입에 불필요한 필드가 포함되어 있습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
