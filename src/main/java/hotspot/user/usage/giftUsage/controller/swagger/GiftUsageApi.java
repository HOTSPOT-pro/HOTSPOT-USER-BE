package hotspot.user.usage.giftUsage.controller.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.usage.giftUsage.controller.response.GiftUsageListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Gift Usage", description = "선물 데이터 사용량 조회 API")
public interface GiftUsageApi {

    @Operation(
            summary = "선물 데이터 사용량 조회",
            description = "현재 회선에서 받은 선물 데이터 사용량 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<hotspot.user.common.ApiResponse<GiftUsageListResponse>> findGiftUsages(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details
    );
}
