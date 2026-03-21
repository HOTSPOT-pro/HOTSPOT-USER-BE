package hotspot.user.weeklyReport.controller.swagger;

import java.time.YearMonth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.weeklyReport.controller.response.MonthlyWeeklyReportResponse;
import hotspot.user.weeklyReport.controller.response.WeeklyReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "AI Report", description = "AI 주간 리포트 열람 API")
public interface WeeklyReportApi {

    @Operation(summary = "구성원별 AI 주간 리포트 상세 조회",
            description = "특정 구성원의 특정 주차 AI 분석 리포트 상세 내용을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "- FAMILY_003: 해당 구성원은 동일한 가족 그룹에 속해 있지 않습니다.\n"
                            + "- WEEKLY_REPORT_002: 요청하신 회선의 리포트가 아닙니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "- FAMILY_002: 가족에 가입된 회선 정보를 찾을 수 없습니다.\n"
                            + "- WEEKLY_REPORT_001: 주간 리포트를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<WeeklyReportResponse>> findWeeklyReport(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal,
            @Parameter(description = "대상 구성원(자녀) 식별자", example = "1000004") @PathVariable Long subId,
            @Parameter(description = "조회할 리포트 식별자", example = "504") @PathVariable Long reportId);

    @Operation(summary = "구성원별 월별 AI 주간 리포트 목록 조회",
            description = "특정 구성원의 특정 월에 생성된 AI 주간 리포트 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "- FAMILY_003: 해당 구성원은 동일한 가족 그룹에 속해 있지 않습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "- FAMILY_002: 가족에 가입된 회선 정보를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ApiResponse<MonthlyWeeklyReportResponse>> findMonthlyWeeklyReports(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principal,
            @Parameter(description = "대상 구성원(자녀) 식별자", example = "1000004") @PathVariable Long subId,
            @Parameter(description = "조회할 월 (yyyy-MM)", example = "2026-03")
            @RequestParam
            @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM")
            YearMonth yearMonth
    );
}
