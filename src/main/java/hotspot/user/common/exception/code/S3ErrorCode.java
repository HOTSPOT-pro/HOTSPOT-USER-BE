package hotspot.user.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum S3ErrorCode implements BaseErrorCode {

    EXTENSION_NOT_PNG(HttpStatus.BAD_REQUEST, "S3_001", "파일 확장자로 PNG만 가능합니다"),
    TEMP_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "S3_002", "임시 버킷에서 해당 파일이 존재하지 않습니다"),
    TEMP_COPY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_003", "임시 버킷 파일 복제 실패"),
    S3_NETWORK_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3_004", "S3 서버 연결 실패"),
    TEMP_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_005", "임시 파일 삭제 실패"),
    PRESIGN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_006", "Presign URL 생성 실패");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}
