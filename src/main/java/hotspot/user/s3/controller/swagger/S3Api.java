package hotspot.user.s3.controller.swagger;

import org.springframework.http.ResponseEntity;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.s3.controller.response.S3PathResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "S3 Image", description = "이미지 업로드 Presigned URL 생성 API")
public interface S3Api {

    @Operation(
            summary = "Presigned URL 생성",
            description = """
                    PNG 이미지 업로드를 위한 Presigned URL을 생성합니다.
                    - 생성된 URL은 5분간 유효합니다.
                    """
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Presigned URL 생성 성공"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = """
                            서버 내부 오류
                            - S3_006: Presign URL 생성 실패
                            """,
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<ApiResponse<S3PathResponse>> createPresignedUrl(
    );
}
