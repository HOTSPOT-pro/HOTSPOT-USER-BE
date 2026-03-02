package hotspot.user.family.controller.swagger;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.family.controller.request.AddFamilyMemberRequest;
import hotspot.user.family.controller.request.CreateNewFamilyRequest;
import hotspot.user.family.controller.response.AddFamilyMemberResponse;
import hotspot.user.family.controller.response.CreateNewFamilyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Family Application", description = "가족 구성원 추가 및 삭제 신청 관련 API")
public interface FamilyApplyApi {

    @Operation(summary = "가족 구성원 추가/삭제 신청",
               description = "가족 OWNER가 새로운 구성원을 추가하거나 기존 구성원을 삭제하기 위한 신청을 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신청 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청\n"
                                         + "- FAMILY_011: 구성원 추가 시 증빙 서류 필수\n"
                                         + "- FAMILY_012: 구성원 추가 시 역할 정보 필수\n"
                                         + "- FAMILY_013: 이미 가족에 소속된 구성원\n"
                                         + "- FAMILY_014: 해당 가족 구성원이 아님",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                                         + "- FAMILY_010: OWNER 권한이 아님",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                                         + "- SUB_001: 회선 정보를 찾을 수 없음",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<CreateNewFamilyResponse>> manageFamilyMember(
            @Valid @RequestBody CreateNewFamilyRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal);

    @Operation(summary = "가족 구성원 추가 신청 (다건)",
               description = "가족 OWNER가 여러 명의 새로운 구성원을 가족으로 추가하기 위해 신청을 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신청 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청\n"
                                         + "- FAMILY_013: 이미 가족에 속해있는 구성원입니다.\n"
                                         + "- FAMILY_017: 타인에게 OWNER 역할을 부여할 수 없습니다.\n"
                                         + "- FAMILY_019: 유효하지 않은 신청 타입입니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음\n"
                                         + "- FAMILY_002: 가족에 가입된 회선 정보를 찾을 수 없습니다.\n"
                                         + "- FAMILY_010: 가족 관리자(OWNER)만 구성원 관리가 가능합니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "찾을 수 없음\n"
                                         + "- SUB_001: 회선 정보를 찾을 수 없습니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "충돌\n"
                                         + "- FAMILY_015: 이미 처리 대기 중인 신청이 존재합니다.",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<AddFamilyMemberResponse>> addFamilyMember(
            @Valid @RequestBody AddFamilyMemberRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal);
}
