package hotspot.user.s3.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.util.s3.S3Util;
import hotspot.user.s3.controller.response.S3PathResponse;

class CreateS3PathServiceImplTest {

    @Test
    @DisplayName("Presigned URL 생성 성공")
    void createS3PathSuccess() {

        // given
        S3Util s3Util = Mockito.mock(S3Util.class);

        Mockito.when(
                s3Util.createPutPresignedUrl(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(Duration.class)
                )
        ).thenReturn("https://mock-url");

        CreateS3PathServiceImpl service =
                new CreateS3PathServiceImpl(s3Util);

        org.springframework.test.util.ReflectionTestUtils
                .setField(service, "tempBucket", "test-bucket");

        // when
        S3PathResponse result =
                service.createS3Path("image/png");

        // then
        assertNotNull(result);
        assertEquals("https://mock-url", result.uploadUrl());
        assertTrue(result.tempKey().endsWith(".png"));
    }

    @Test
    @DisplayName("PNG 아닌 경우 예외 발생")
    void createS3PathFailInvalidType() {

        // given
        S3Util s3Util = Mockito.mock(S3Util.class);

        CreateS3PathServiceImpl service =
                new CreateS3PathServiceImpl(s3Util);

        org.springframework.test.util.ReflectionTestUtils
                .setField(service, "tempBucket", "test-bucket");

        // when & then
        assertThrows(ApplicationException.class,
                () -> service.createS3Path("image/jpeg"));
    }
}
