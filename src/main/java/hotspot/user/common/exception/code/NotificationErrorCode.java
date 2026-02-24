package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI_001", "알림 정보를 찾을 수 없습니다."),
    NOTIFICATION_CATEGORY_MAPPING_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "NOTI_002",
            "알림 타입에 대한 카테고리 매핑 정보를 찾을 수 없습니다."
    ),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
