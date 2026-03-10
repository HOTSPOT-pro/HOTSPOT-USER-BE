package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TotalUsageErrorCode implements BaseErrorCode {

    PLAN_DATA_LIMIT_NOT_FOUND(HttpStatus.BAD_REQUEST, "TOTAL_USAGE_001", "개인 요금제 데이터 한도를 조회할 수 없습니다"),
    FAMILY_DATA_LIMIT_NOT_FOUND(HttpStatus.BAD_REQUEST, "TOTAL_USAGE_002", "가족 요금제 데이터 한도를 조회할 수 없습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
