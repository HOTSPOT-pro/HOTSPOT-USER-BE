package hotspot.user.policy.controller.swagger;

import java.util.List;

import org.springframework.http.ResponseEntity;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Policy", description = "차단 정책 관련 API")
public interface BlockPolicyApi {

    @Operation(summary = "전체 정책 목록 조회", description = "시스템에 정의된 모든 차단 정책(앱 차단, 시간 제한 등) 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패\n"
                                     + "- AUTH_001: 유효하지 않은 토큰\n"
                                     + "- AUTH_002: 만료된 토큰",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                                     + "- AUTH_004: 온보딩 미완료 유저(PENDING)",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<List<BlockPolicyResponse>>> getAllPolicies();
}
