package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserAlertConsumerErrorCode implements BaseErrorCode {
    TARGET_RESOLUTION_FAILED(
            HttpStatus.BAD_REQUEST,
            "UAC_001",
            "사용자 알림 대상 해석에 실패했습니다."
    ),
    NOTIFICATION_MAPPING_FAILED(
            HttpStatus.BAD_REQUEST,
            "UAC_002",
            "사용자 알림 매핑에 실패했습니다."
    ),
    NOTIFICATION_TYPE_RESOLUTION_FAILED(
            HttpStatus.BAD_REQUEST,
            "UAC_003",
            "사용자 알림 타입 해석에 실패했습니다."
    ),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
