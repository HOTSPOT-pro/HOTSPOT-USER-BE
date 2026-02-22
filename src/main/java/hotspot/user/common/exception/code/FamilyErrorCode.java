package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 가족 도메인 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum FamilyErrorCode implements BaseErrorCode {
    FAMILY_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_001", "가족 정보를 찾을 수 없습니다."),
    FAMILY_SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_002", "가족에 가입된 회선 정보를 찾을 수 없습니다."),
    NOT_FAMILY_MEMBER(HttpStatus.FORBIDDEN, "FAMILY_003", "해당 구성원은 동일한 가족 그룹에 속해 있지 않습니다."),
    INVALID_DATA_LIMIT(HttpStatus.BAD_REQUEST, "FAMILY_004", "데이터 한도는 -1(무한대) 이상이어야 합니다."),
    BLOCKED_SERVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_005", "차단 대상 서비스 정보 중 일부를 찾을 수 없습니다."),
    INVALID_PRIORITY_VALUE(HttpStatus.BAD_REQUEST, "FAMILY_006", "우선순위 모드에서는 -1 값을 사용할 수 없습니다."),
    DUPLICATE_PRIORITY(HttpStatus.BAD_REQUEST, "FAMILY_007", "중복된 우선순위 값이 존재합니다."),
    NOT_CONTINUOUS_PRIORITY(HttpStatus.BAD_REQUEST, "FAMILY_008", "우선순위는 1부터 시작하여 연속적이어야 합니다."),
    MISSING_PRIORITY_VALUES(HttpStatus.BAD_REQUEST, "FAMILY_009", "모든 가족 구성원의 우선순위 값이 필요합니다."),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
