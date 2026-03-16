package hotspot.user.policy.controller.swagger;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.policy.controller.request.UpdatePolicySubRequest;
import hotspot.user.policy.controller.response.BlockedStatusResponse;
import hotspot.user.policy.controller.response.UpdatePolicySubResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Applied Policy", description = "적용된 정책(시간 정책 및 앱 차단) 관리 API")
public interface AppliedPolicyApi {

    @Operation(summary = "적용된 정책 목록 조회", description = "가족 전체의 적용 정책 또는 본인의 적용 정책을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                                         + "- AUTH_004: 해당 요청에 대한 접근 권한이 없습니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<Object>> getAppliedPolicies(
            @Parameter(description = "true일 경우 가족 전체 조회, false일 경우 본인 정책 조회", example = "false")
            @RequestParam(defaultValue = "false") boolean isFamily,

            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails principal
    );

    @Operation(summary = "구성원별 정책 업데이트 (적용)",
               description = "가족 OWNER가 특정 구성원(회선)에게 시간 정책 및 앱 차단 정책을 적용합니다. "
                           + "관리자 정책 또는 우리 가족이 직접 만든 정책만 사용할 수 있습니다. "
                           + "활성화할 정책 ID 리스트를 전달하며, 빈 리스트를 보내면 모두 해제됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "정책 업데이트 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청\n"
                                         + "- COMMON_002: 입력값 유효성 검증 실패 (null 등)\n"
                                         + "- POLICY_003: 현재 비활성화된 정책은 적용할 수 없습니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                                         + "- AUTH_004: 해당 요청에 대한 접근 권한이 없습니다.\n"
                                         + "- FAMILY_003: 해당 구성원은 동일한 가족 그룹에 속해 있지 않습니다.\n"
                                         + "- POLICY_002: 관리자 또는 우리 가족이 직접 만든 정책만 사용할 수 있습니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                                         + "- FAMILY_002: 가족에 가입된 회선 정보를 찾을 수 없습니다.\n"
                                         + "- POLICY_001: 해당 정책을 찾을 수 없습니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<UpdatePolicySubResponse>> updatePolicySub(
            @Valid @RequestBody UpdatePolicySubRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    );

    @Operation(summary = "나의 데이터 사용 차단 여부 조회", description = "현재 나의 실시간 데이터 차단 여부와 사유(정책)를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                                         + "- MEMBER_001: 회원 정보를 찾을 수 없습니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<BlockedStatusResponse>> getBlockedStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    );

    @Operation(summary = "구성원별 데이터 사용 불가능한 시간대 조회", description = "가족 전체 또는 본인의 요일별 차단 시간대 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                                         + "- AUTH_004: 해당 요청에 대한 접근 권한이 없습니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<Object>> getBlockedTime(
            @Parameter(description = "true일 경우 가족 전체 조회, false일 경우 본인 정책 조회", example = "false")
            @RequestParam(defaultValue = "false") boolean isFamily,

            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails principal
    );
}
