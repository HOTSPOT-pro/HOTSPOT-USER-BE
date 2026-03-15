package hotspot.user.familyReport.controller.swagger;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.familyReport.controller.request.CreateFamilyReportSubscriptionRequest;
import hotspot.user.familyReport.controller.response.FamilyReportSubscriptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "AI Report", description = "AI 주간 리포트 구독 API")
public interface FamilyReportApi {

    @Operation(summary = "가족 AI 리포트 구독 여부 조회",
            description = "현재 로그인한 사용자가 속한 가족의 AI 주간 리포트 구독 여부를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<FamilyReportSubscriptionResponse>> findFamilyReportSubscription(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal);

    @Operation(summary = "가족 AI 리포트 구독 신청",
            description = "가족 OWNER가 AI 주간 리포트 구독을 신청하고 수신 요일을 설정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<Void>> createFamilyReportSubscription(
            @Valid @RequestBody CreateFamilyReportSubscriptionRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal);
}
