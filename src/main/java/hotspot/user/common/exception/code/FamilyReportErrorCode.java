package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FamilyReportErrorCode implements BaseErrorCode {
    RECEIVE_DAY_REQUIRED(HttpStatus.BAD_REQUEST, "FAMILY_REPORT_001", "리포트 수신 요일은 필수입니다."),
    FAMILY_REPORT_SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_REPORT_002", "가족 AI 리포트 구독 정보를 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
