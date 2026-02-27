package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 데이터 선물하기 도메인 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum PresentDataErrorCode implements BaseErrorCode {
    PRESENT_DATA_SELF_GIFT(HttpStatus.BAD_REQUEST, "PRESENT_001", "자신에게는 데이터를 선물할 수 없습니다."),
    PRESENT_DATA_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "PRESENT_002", "데이터 선물은 1GB에서 5GB 사이, 1GB 단위로만 가능합니다."),
    NOT_ENOUGH_DATA(HttpStatus.BAD_REQUEST, "PRESENT_003", "남은 데이터 양보다 선물하려는 데이터 양이 많을 수 없습니다"),
    MONTHLY_GIFT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PRESENT_004", "월별 선물 데이터 한도량을 초과하였습니다")
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
