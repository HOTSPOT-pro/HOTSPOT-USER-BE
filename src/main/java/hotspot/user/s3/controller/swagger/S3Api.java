package hotspot.user.s3.controller.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.s3.controller.response.S3PathResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "S3 Image", description = "이미지 업로드 Presigned URL 생성 API")
public interface S3Api {

    @Operation(
            summary = "Presigned URL 생성",
            description = "PNG 이미지 업로드를 위한 Presigned URL을 생성합니다.\n"
                    + "- contentType은 반드시 image/png 이어야 합니다.\n"
                    + "- 생성된 URL은 5분간 유효합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Presigned URL 생성 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PNG 파일이 아닌 경우\n"
                            + "- S3_001: PNG 파일만 업로드 가능합니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<S3PathResponse>> createPresignedUrl(

            @Parameter(
                    description = "파일 Content-Type (image/png 고정)",
                    example = "image/png",
                    required = true
            )
            @RequestParam String contentType
    );
}
