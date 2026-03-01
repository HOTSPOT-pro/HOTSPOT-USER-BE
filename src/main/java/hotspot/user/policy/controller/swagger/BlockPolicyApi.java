package hotspot.user.policy.controller.swagger;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.policy.controller.request.UpdateFamilyBlockPolicyStatusRequest;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.controller.response.UpdateFamilyBlockPolicyStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Policy", description = "차단 정책 관련 API")
public interface BlockPolicyApi {

    @Operation(summary = "전체 관리자 정책 목록 조회", description = "시스템 관리자가 생성한 공용 차단 정책(isActive=true) 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패\n"
                                         + "- AUTH_002: 유효하지 않은 토큰입니다.\n"
                                         + "- AUTH_006: 토큰이 만료되었습니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    ResponseEntity<ApiResponse<List<BlockPolicyResponse>>> getAllPolicies();

    @Operation(summary = "우리 가족 정책 목록 조회", description = "우리 가족 구성원이 생성한 정책 목록을 조회합니다. OWNER 또는 PARENT 권한이 필요합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                                         + "- FAMILY_003: 해당 구성원은 동일한 가족 그룹에 속해 있지 않습니다.\n"
                                         + "- AUTH_004: 해당 요청에 대한 접근 권한이 없습니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                                         + "- FAMILY_002: 가족에 가입된 회선 정보를 찾을 수 없습니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패\n"
                                         + "- AUTH_002: 유효하지 않은 토큰입니다.\n"
                                         + "- AUTH_006: 토큰이 만료되었습니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/families")
    ResponseEntity<ApiResponse<List<BlockPolicyResponse>>> getFamilyPolicies(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails principal
    );

    @Operation(summary = "우리 가족 정책 상태 업데이트 (비/활성화)",
            description = "우리 가족이 생성한 정책들의 활성화 상태를 일괄 업데이트합니다. OWNER 권한이 필요합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                    + "- FAMILY_003: 해당 구성원은 동일한 가족 그룹에 속해 있지 않습니다.\n"
                    + "- AUTH_004: 해당 요청에 대한 접근 권한이 없습니다.\n"
                    + "- POLICY_002: 관리자 또는 우리 가족이 직접 만든 정책만 사용할 수 있습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                    + "- MEMBER_001: 회원 정보를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/families")
    ResponseEntity<ApiResponse<UpdateFamilyBlockPolicyStatusResponse>> updateFamilyBlockPolicies(
            @RequestBody UpdateFamilyBlockPolicyStatusRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails principal
    );

    @Operation(summary = "우리 가족 정책 삭제",
            description = "우리 가족이 생성한 정책들을 삭제(Soft Delete)합니다. OWNER 권한이 필요합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                    + "- FAMILY_003: 해당 구성원은 동일한 가족 그룹에 속해 있지 않습니다.\n"
                    + "- AUTH_004: 해당 요청에 대한 접근 권한이 없습니다.\n"
                    + "- POLICY_002: 관리자 또는 우리 가족이 직접 만든 정책만 사용할 수 있습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                    + "- MEMBER_001: 회원 정보를 찾을 수 없습니다.\n"
                    + "- POLICY_001: 해당 정책을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping
    ResponseEntity<ApiResponse<Void>> deleteFamilyBlockPolicies(
            @Parameter(description = "삭제할 정책 ID 리스트")
            @RequestParam List<Long> policyIdList,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails principal
    );
    @Operation(summary = "정책 단일 조회",
            description = "특정 정책의 상세 정보를 조회합니다. OWNER 권한이 필요하며, 관리자 정책 또는 본인 가족의 정책만 조회 가능합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                    + "- AUTH_004: 해당 요청에 대한 접근 권한이 없습니다.\n"
                    + "- POLICY_002: 관리자 또는 우리 가족이 직접 만든 정책만 사용할 수 있습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                    + "- POLICY_001: 해당 정책을 찾을 수 없습니다.\n"
                    + "- MEMBER_001: 회원 정보를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{blockPolicyId}")
    ResponseEntity<ApiResponse<BlockPolicyResponse>> getBlockPolicy(
            @PathVariable Long blockPolicyId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails principal
    );
}
