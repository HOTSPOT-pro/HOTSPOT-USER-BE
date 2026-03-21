package hotspot.user.s3.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.s3.controller.port.CreateS3PathService;
import hotspot.user.s3.controller.response.S3PathResponse;
import hotspot.user.s3.controller.swagger.S3Api;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/image")
public class S3Controller implements S3Api {

    private final CreateS3PathService createS3PathService;

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<S3PathResponse>> createPresignedUrl(
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        createS3PathService.createS3Path()
                )
        );
    }
}
