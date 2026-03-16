package hotspot.user.weeklyReport.controller.port;

import java.time.YearMonth;

import hotspot.user.weeklyReport.controller.response.MonthlyWeeklyReportResponse;

public interface FindMonthlyWeeklyReportService {
    MonthlyWeeklyReportResponse findMonthlyWeeklyReports(Long requesterMemberId, Long subId, YearMonth yearMonth);
}
