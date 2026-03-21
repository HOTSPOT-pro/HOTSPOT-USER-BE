package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportUsageErrorCode implements BaseErrorCode{

    TARGET_SUBSCRIPTION_NOT_IN_FAMILY(HttpStatus.NOT_FOUND, "REPORT_USAGE_001", "조회하려는 회선이 같은 가족에 존재하지 않습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
