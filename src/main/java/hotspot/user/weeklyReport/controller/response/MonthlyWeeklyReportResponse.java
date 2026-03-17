package hotspot.user.weeklyReport.controller.response;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import hotspot.user.weeklyReport.domain.ReportStatus;
import lombok.Builder;

@Builder
public record MonthlyWeeklyReportResponse(
        Long subId,
        String name,
        YearMonth yearMonth,
        List<ReportItem> reports
) {

    @Builder
    public record ReportItem(
            Long reportId,
            String title,
            String period,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            ReportStatus reportStatus
    ) {
    }
}
