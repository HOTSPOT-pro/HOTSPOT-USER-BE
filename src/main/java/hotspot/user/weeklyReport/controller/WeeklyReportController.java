package hotspot.user.weeklyReport.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.weeklyReport.controller.port.FindWeeklyReportService;
import hotspot.user.weeklyReport.controller.response.WeeklyReportResponse;
import hotspot.user.weeklyReport.controller.swagger.WeeklyReportApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai-reports")
public class WeeklyReportController implements WeeklyReportApi {

    private final FindWeeklyReportService findWeeklyReportService;

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
}
