package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SmsErrorCode implements BaseErrorCode {
    UNSUPPORTED_SMS_NOTIFICATION_TYPE(
            HttpStatus.BAD_REQUEST,
            "SMS_001",
            "SMS 발송 대상이 아닌 알림 타입입니다."
    ),
    SMS_NOTIFICATION_TYPE_NOT_TARGET(
            HttpStatus.BAD_REQUEST,
            "SMS_002",
            "SMS 발송 대상 알림 타입이 아닙니다."
    ),
    SMS_NOTIFICATION_ALLOW_DISABLED(
            HttpStatus.FORBIDDEN,
            "SMS_003",
            "데이터 알림 수신 설정이 비활성화되어 SMS를 발송할 수 없습니다."
    ),
    SMS_RECIPIENT_SUBSCRIPTION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SMS_004",
            "SMS 수신자의 구독 정보를 찾을 수 없습니다."
    ),
    SMS_RECIPIENT_PHONE_MISSING(
            HttpStatus.BAD_REQUEST,
            "SMS_005",
            "SMS 수신자의 전화번호 정보가 없습니다."
    ),
    SMS_RECIPIENT_DECRYPT_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "SMS_006",
            "SMS 수신자 전화번호 복호화에 실패했습니다."
    ),
    SMS_DISPATCH_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "SMS_007",
            "SMS 발송 처리 중 오류가 발생했습니다."
    ),
    SMS_LISTENER_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "SMS_008",
            "SMS 리스너 처리 중 오류가 발생했습니다."
    ),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
