package hotspot.user.s3.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.S3ErrorCode;
import hotspot.user.common.util.s3.S3Util;
import hotspot.user.s3.controller.port.CreateS3PathService;
import hotspot.user.s3.controller.response.S3PathResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateS3PathServiceImpl implements CreateS3PathService {

    private final S3Util s3Util;

    @Value("${s3.bucket.temp}")
    private String tempBucket;

    private static final String ALLOWED_TYPE = "image/png";

    /**
     * 파일 확장자 검증 (PNG만 허용)
     * Presigned URL 생성
     */
    @Override
    public S3PathResponse createS3Path(String contentType) {

        validateContentType(contentType);

        String tempKey = generateTempKey();

        String presignedUrl = s3Util.createPutPresignedUrl(
                tempBucket,
                tempKey,
                ALLOWED_TYPE,
                Duration.ofMinutes(5)
        );

        return new S3PathResponse(
                presignedUrl,
                tempKey
        );
    }

    private String generateTempKey() {
        return "temp/"
                + LocalDate.now()
                + "/"
                + UUID.randomUUID()
                + ".png";
    }

    private void validateContentType(String contentType) {
        if (!ALLOWED_TYPE.equals(contentType)) {
            throw new ApplicationException(S3ErrorCode.EXTENSION_NOT_PNG);
        }
    }
}
