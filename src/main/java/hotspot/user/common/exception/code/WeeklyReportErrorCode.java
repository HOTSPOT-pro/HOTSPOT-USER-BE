package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeeklyReportErrorCode implements BaseErrorCode {
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "WEEKLY_REPORT_001", "주간 리포트를 찾을 수 없습니다."),
    REPORT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "WEEKLY_REPORT_002", "요청하신 회선의 리포트가 아닙니다."),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
