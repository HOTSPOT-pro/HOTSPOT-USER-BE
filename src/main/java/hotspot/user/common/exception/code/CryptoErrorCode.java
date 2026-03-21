package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CryptoErrorCode implements BaseErrorCode {
    SUBSCRIPTION_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "CRYPTO_001", "회선 복호화 키 정보를 찾을 수 없습니다."),
    SECRET_KEY_REQUIRED(HttpStatus.INTERNAL_SERVER_ERROR, "CRYPTO_002", "로컬 복호화용 SECRET_KEY가 필요합니다."),
    INVALID_SECRET_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "CRYPTO_003", "SECRET_KEY 형식이 올바르지 않습니다."),
    INVALID_ENCRYPTED_PAYLOAD(HttpStatus.BAD_REQUEST, "CRYPTO_004", "암호화 데이터 형식이 올바르지 않습니다."),
    DECRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CRYPTO_005", "전화번호 복호화에 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
