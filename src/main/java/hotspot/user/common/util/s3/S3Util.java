package hotspot.user.common.util.s3;

import java.net.URL;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.S3ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Util {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${s3.bucket.temp}")
    private String tempBucket;

    @Value("${s3.bucket.certificate}")
    private String certificateBucket;


    /**
     * PresignedUrl 생성
     */
    public String createPutPresignedUrl(String bucket, String key,
                                        String contentType,
                                        Duration duration) {

        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest =
                    PutObjectPresignRequest.builder()
                            .signatureDuration(duration)
                            .putObjectRequest(objectRequest)
                            .build();

            URL url = s3Presigner.presignPutObject(presignRequest).url();
            return url.toString();

        } catch (Exception e) {
            log.error("Presigned URL 생성 실패", e);
            throw new ApplicationException(S3ErrorCode.PRESIGN_FAILED);
        }
    }

    /**
     * Temp(임시 버킷)에서 Certificate(메인 버킷)으로 해당 파일 복제
     * Temp(임시 버킷)에 있던 기존 파일은 제거
     */

    public String moveTempToCertificate(String tempKey) {

        String newKey = tempKey.replace("temp/", "certificate/");

        try {
            copy(tempBucket, tempKey, certificateBucket, newKey);
            delete(tempBucket, tempKey);
            return newKey;

        } catch (NoSuchKeyException e) {
            log.warn("Temp file not found: {}", tempKey);
            throw new ApplicationException(S3ErrorCode.TEMP_FILE_NOT_FOUND);

        } catch (S3Exception e) {
            log.error("S3 error. statusCode={}, message={}",
                    e.statusCode(),
                    e.awsErrorDetails() != null
                            ? e.awsErrorDetails().errorMessage()
                            : e.getMessage());
            throw new ApplicationException(S3ErrorCode.TEMP_COPY_FAILED);

        } catch (SdkException e) {
            log.error("S3 SDK error", e);
            throw new ApplicationException(S3ErrorCode.S3_NETWORK_ERROR);
        }
    }

    private void copy(String sourceBucket, String sourceKey,
                      String targetBucket, String targetKey) {

        CopyObjectRequest request = CopyObjectRequest.builder()
                .sourceBucket(sourceBucket)
                .sourceKey(sourceKey)
                .destinationBucket(targetBucket)
                .destinationKey(targetKey)
                .build();

        s3Client.copyObject(request);
    }

    private void delete(String bucket, String key) {

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }
}
