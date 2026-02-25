package hotspot.user.usage.reportUsage.controller.swagger;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ErrorResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.usage.reportUsage.controller.response.ReportFamilyResponse;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageAppResponse;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageDayResponse;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageMonthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Report Usage", description = "가족 및 앱 데이터 사용량 리포트 조회 API")
public interface ReportUsageApi {

    @Operation(
            summary = "가족 구성원 목록 조회",
            description = "사용자의 가족 구성원 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<List<ReportFamilyResponse>>> findReportFamily(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details
    );


    @Operation(
            summary = "앱 월별 사용량 조회",
            description = "현재 로그인한 사용자의 특정 회선 앱 월별 사용량을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 회선을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<ReportUsageAppResponse>> findReportUsageAppMonth(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details,

            @Parameter(description = "조회 대상 회선 ID", example = "10")
            @RequestParam Long targetSubId
    );


    @Operation(
            summary = "앱 일별 사용량 조회",
            description = "현재 로그인한 사용자의 특정 회선 앱 일별 사용량을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 회선을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<ReportUsageAppResponse>> findReportUsageAppDay(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details,

            @Parameter(description = "조회 대상 회선 ID", example = "10")
            @RequestParam Long targetSubId
    );


    @Operation(
            summary = "가족 일별 사용량 리포트 조회",
            description = "본인 및 가족 구성원의 일별 데이터 사용량을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "가족에서 targetSubId를 찾을 수 없음\n"
                            + "- REPORT_USAGE_001: 조회하려는 회선이 같은 가족에 존재하지 않습니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<ReportUsageDayResponse>> findReportUsageDay(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details,

            @Parameter(description = "조회 대상 회선 ID (전체 선택 시 NULL)", example = "10")
            @RequestParam(required = false) Long targetSubId
    );


    @Operation(
            summary = "가족 월별 사용량 리포트 조회",
            description = "본인 및 가족 구성원의 최근 6개월 월별 데이터 사용량을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "가족에서 targetSubId를 찾을 수 없음\n"
                            + "- REPORT_USAGE_001: 조회하려는 회선이 같은 가족에 존재하지 않습니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApiResponse<ReportUsageMonthResponse>> findReportUsageMonth(
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalDetails details,

            @Parameter(description = "조회 대상 회선 ID (전체 선택 시 NULL)", example = "10")
            @RequestParam(required = false) Long targetSubId
    );
}
