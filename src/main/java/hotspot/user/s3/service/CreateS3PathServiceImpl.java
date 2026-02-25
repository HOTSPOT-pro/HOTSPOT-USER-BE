package hotspot.user.s3.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.S3ErrorCode;
import hotspot.user.s3.controller.port.CreateS3PathService;
import hotspot.user.s3.controller.response.S3PathResponse;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class CreateS3PathServiceImpl implements CreateS3PathService {

    private final S3Presigner s3Presigner;

    @Value("${s3.bucket.temp}")
    private String tempBucket;

    private static final String ALLOWED_TYPE = "image/png";

    @Override
    public S3PathResponse createS3Path(String contentType) {

        validateContentType(contentType);

        String tempKey = "temp/"
                + LocalDate.now()
                + "/"
                + UUID.randomUUID()
                + ".png";

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(tempBucket)
                .key(tempKey)
                .contentType(ALLOWED_TYPE)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .putObjectRequest(objectRequest)
                        .build();

        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(presignRequest);

        return new S3PathResponse(
                presignedRequest.url().toString(),
                tempKey
        );
    }

    private void validateContentType(String contentType) {
        if (!ALLOWED_TYPE.equals(contentType)) {
            throw new ApplicationException(S3ErrorCode.EXTENSION_NOT_PNG);
        }
    }
}
