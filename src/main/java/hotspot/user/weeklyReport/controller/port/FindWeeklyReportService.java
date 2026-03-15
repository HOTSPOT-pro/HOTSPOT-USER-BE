package hotspot.user.weeklyReport.controller.port;

import hotspot.user.weeklyReport.controller.response.WeeklyReportResponse;

public interface FindWeeklyReportService {

    WeeklyReportResponse findWeeklyReport(Long memberId, Long subId, Long reportId);
}
