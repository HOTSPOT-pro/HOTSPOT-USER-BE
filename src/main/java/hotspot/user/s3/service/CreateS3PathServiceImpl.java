package hotspot.user.s3.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Override
    public S3PathResponse createS3Path() {

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
}
