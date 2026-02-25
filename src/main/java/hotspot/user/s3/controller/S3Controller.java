package hotspot.user.s3.controller;

import hotspot.user.s3.controller.swagger.S3Api;
import org.springframework.http.ResponseEntity;

import hotspot.user.common.ApiResponse;
import hotspot.user.s3.controller.port.CreateS3PathService;
import hotspot.user.s3.controller.response.S3PathResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/image")
public class S3Controller implements S3Api {

    private final CreateS3PathService createS3PathService;

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<S3PathResponse>> createPresignedUrl(
            @RequestParam String contentType
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        createS3PathService.createS3Path(contentType)
                )
        );
    }
}
