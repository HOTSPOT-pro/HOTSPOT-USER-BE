package hotspot.user.usage.reportUsage.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.usage.reportUsage.controller.port.*;
import hotspot.user.usage.reportUsage.controller.response.ReportFamilyResponse;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageAppResponse;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageDayResponse;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageMonthResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reportUsage")
public class ReportUsageController {

    private final FindReportUsageAppMonthService findReportUsageAppMonthService;
    private final FindReportUsageAppDayService findReportUsageAppDayService;
    private final FindReportUsageDayService findReportUsageDayService;
    private final FindReportUsageMonthService findReportUsageMonthService;
    private final FindReportFamilyService findReportFamilyService;

    @GetMapping("/family")
    public ResponseEntity<ApiResponse<List<ReportFamilyResponse>>> findReportFamily(
            @AuthenticationPrincipal PrincipalDetails details) {
        return ResponseEntity.ok(ApiResponse.success(
                findReportFamilyService.findReportFamily(details.getFamilyId())));
    }

    @GetMapping("/app/month")
    public ResponseEntity<ApiResponse<ReportUsageAppResponse>> findReportUsageAppMonth(
            @AuthenticationPrincipal PrincipalDetails details,
            @RequestParam Long targetSubId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        findReportUsageAppMonthService
                                .findReportUsageAppMonth(details.getId(), targetSubId)));
    }

    @GetMapping("/app/day")
    public ResponseEntity<ApiResponse<ReportUsageAppResponse>> findReportUsageAppDay(
            @AuthenticationPrincipal PrincipalDetails details,
            @RequestParam Long targetSubId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        findReportUsageAppDayService
                                .findReportUsageAppDay(details.getId(), targetSubId)
                )
        );
    }

    @GetMapping("/day")
    public ResponseEntity<ApiResponse<ReportUsageDayResponse>> findReportUsageDay(
            @AuthenticationPrincipal PrincipalDetails details,
            @RequestParam(required = false) Long targetSubId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        findReportUsageDayService.findReportUsageDay(
                                details.getFamilyId(),
                                targetSubId
                        )
                )
        );
    }

    @GetMapping("/month")
    public ResponseEntity<ApiResponse<ReportUsageMonthResponse>> findReportUsageMonth(
            @AuthenticationPrincipal PrincipalDetails details,
            @RequestParam(required = false) Long targetSubId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        findReportUsageMonthService.findReportUsageMonth(
                                details.getFamilyId(),
                                targetSubId
                        )
                )
        );
    }
}
