package hotspot.user.s3.service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.s3.controller.response.S3PathResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;


class CreateS3PathServiceImplTest {

    @Test
    @DisplayName("Presigned URL 생성 성공")
    void createS3Path_success() throws Exception {

        S3Presigner presigner = Mockito.mock(S3Presigner.class);

        PresignedPutObjectRequest mockRequest =
                Mockito.mock(PresignedPutObjectRequest.class);

        Mockito.when(mockRequest.url())
                .thenReturn(new URL("https://mock-url"));

        Mockito.when(
                presigner.presignPutObject(any(PutObjectPresignRequest.class))
        ).thenReturn(mockRequest);

        CreateS3PathServiceImpl service =
                new CreateS3PathServiceImpl(presigner);

        // tempBucket 필드 수동 주입
        org.springframework.test.util.ReflectionTestUtils
                .setField(service, "tempBucket", "test-bucket");

        S3PathResponse result =
                service.createS3Path("image/png");

        assertNotNull(result);
        assertEquals("https://mock-url", result.uploadUrl());
        assertTrue(result.tempKey().endsWith(".png"));
    }

    @Test
    @DisplayName("PNG 아닌 경우 예외 발생")
    void createS3Path_fail_invalidType() {

        S3Presigner presigner = Mockito.mock(S3Presigner.class);

        CreateS3PathServiceImpl service =
                new CreateS3PathServiceImpl(presigner);

        org.springframework.test.util.ReflectionTestUtils
                .setField(service, "tempBucket", "test-bucket");

        assertThrows(ApplicationException.class,
                () -> service.createS3Path("image/jpeg"));
    }
}