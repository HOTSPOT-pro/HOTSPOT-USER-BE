package hotspot.user.family.controller.swagger;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.family.controller.request.UpdateDataLimitRequest;
import hotspot.user.family.controller.request.UpdateFamilyPriorityRequest;
import hotspot.user.family.controller.request.UpdateFamilyRoleRequest;
import hotspot.user.family.controller.response.FindDataLimitResponse;
import hotspot.user.family.controller.response.UpdateDataLimitResponse;
import hotspot.user.family.controller.response.UpdateFamilyPriorityResponse;
import hotspot.user.family.controller.response.UpdateFamilyRoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Family Subscription", description = "가족 회선 및 데이터 한도 관리 API")
public interface FamilySubscriptionApi {

    @Operation(summary = "구성원 데이터 한도 수정",
               description = "가족 OWNER가 특정 구성원의 데이터 공유 한도를 수정합니다. "
                           + "본인이 속한 가족의 구성원만 수정 가능하며, 한도는 -1 이상이어야 합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청\n"
                                         + "- COMMON_002: 입력값 유효성 검증 실패\n"
                                         + "- FAMILY_004: 데이터 한도 범위 오류 (-1 미만)",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                                         + "- AUTH_004: OWNER 권한이 아님\n"
                                         + "- FAMILY_003: 동일 가족 구성원이 아님",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                                         + "- FAMILY_002: 해당 회선(Subscription)이 가족에 등록되어 있지 않음",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<UpdateDataLimitResponse>> updateDataLimit(
            @Valid @RequestBody UpdateDataLimitRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal);

    @Operation(summary = "가족 우선순위 정책 수정",
               description = "가족 OWNER가 가족 전체의 우선순위 방식(FIFO/PRIORITY) 및 구성원별 순위를 수정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청\n"
                                         + "- FAMILY_007: 중복된 우선순위 값 존재\n" + "- FAMILY_008: 우선순위 값이 연속적이지 않음\n"
                                         + "- FAMILY_009: 일부 구성원의 우선순위 값 누락",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                                         + "- AUTH_004: OWNER 권한이 아님\n"
                                         + "- FAMILY_003: 동일 가족 구성원이 아님",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가족 정보를 찾을 수 없음",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<UpdateFamilyPriorityResponse>> updateFamilyPriority(
            @Valid @RequestBody UpdateFamilyPriorityRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal);

    @Operation(summary = "구성원 역할 수정",
               description = "가족 OWNER가 특정 구성원의 역할(PARENT/CHILD)을 수정합니다. "
                           + "본인의 역할은 수정할 수 없으며, 같은 가족 구성원만 수정 가능합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청\n"
                                         + "- COMMON_002: 입력값 유효성 검증 실패\n"
                                         + "- FAMILY_016: 본인 역할 수정 시도\n"
                                         + "- FAMILY_017: 타인에게 OWNER 역할 부여 시도",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                                         + "- AUTH_004: OWNER 권한이 아님\n"
                                         + "- FAMILY_003: 동일 가족 구성원이 아님",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                                         + "- FAMILY_002: 해당 회선 정보를 찾을 수 없음",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<UpdateFamilyRoleResponse>> updateFamilyRole(
            @Parameter(description = "대상 회선 ID", example = "10") @PathVariable Long subId,
            @Valid @RequestBody UpdateFamilyRoleRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal);

    @Operation(summary = "구성원 데이터 한도 정보 조회",
               description = "가족 OWNER가 특정 구성원의 데이터 한도 정보 및 차단 상태를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패\n"
                                         + "- AUTH_002: 유효하지 않은 토큰\n"
                                         + "- AUTH_006: 토큰 만료",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                                         + "- FAMILY_010: 가족 관리자(OWNER)만 조회 가능합니다.\n"
                                         + "- FAMILY_003: 동일 가족 구성원이 아닙니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                                         + "- FAMILY_002: 가족에 가입된 회선 정보를 찾을 수 없습니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<FindDataLimitResponse>> findDataLimit(
            @Parameter(description = "대상 회선 ID", example = "10") @PathVariable Long subId,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal);
}
