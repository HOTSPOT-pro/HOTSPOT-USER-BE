package hotspot.user.usage.totalUsage.controller.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.usage.totalUsage.controller.response.TotalUsageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Total Usage", description = "전체 데이터 사용량 조회 API")
public interface TotalUsageApi {

    @Operation(
            summary = "전체 데이터 조회",
            description = """
                    개인 데이터 + 선물 데이터 + 가족 데이터 잔여량을 합산한
                    전체 데이터 사용량을 조회합니다.
                    """
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = """
                            잘못된 요청
                            - TOTAL_USAGE_001: 개인 요금제 데이터 한도를 조회할 수 없습니다
                            - TOTAL_USAGE_002: 가족 요금제 데이터 한도를 조회할 수 없습니다
                            - MEMBER_004: 회선의 가족 결합 정보를 찾을 수 없습니다
                            """,
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    ResponseEntity<hotspot.user.common.ApiResponse<TotalUsageResponse>> findTotalUsage(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details
    );
}
