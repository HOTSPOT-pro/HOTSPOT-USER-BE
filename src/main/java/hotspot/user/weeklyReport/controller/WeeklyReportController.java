package hotspot.user.weeklyReport.controller;

import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.weeklyReport.controller.port.FindMonthlyWeeklyReportService;
import hotspot.user.weeklyReport.controller.port.FindWeeklyReportService;
import hotspot.user.weeklyReport.controller.response.MonthlyWeeklyReportResponse;
import hotspot.user.weeklyReport.controller.response.WeeklyReportResponse;
import hotspot.user.weeklyReport.controller.swagger.WeeklyReportApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai-reports")
public class WeeklyReportController implements WeeklyReportApi {

    private final FindWeeklyReportService findWeeklyReportService;
    private final FindMonthlyWeeklyReportService findMonthlyWeeklyReportService;

    @Override
    @GetMapping("/families/members/{subId}/reports/{reportId}")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> findWeeklyReport(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long subId,
            @PathVariable Long reportId) {

        WeeklyReportResponse response = findWeeklyReportService.findWeeklyReport(
                principal.getId(),
                subId,
                reportId
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @GetMapping("/families/members/{subId}/monthly")
    public ResponseEntity<ApiResponse<MonthlyWeeklyReportResponse>> findMonthlyWeeklyReports(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long subId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

        MonthlyWeeklyReportResponse response = findMonthlyWeeklyReportService.findMonthlyWeeklyReports(
                principal.getId(),
                subId,
                yearMonth
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
