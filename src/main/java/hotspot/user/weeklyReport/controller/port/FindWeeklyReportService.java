package hotspot.user.weeklyReport.controller.port;

import hotspot.user.weeklyReport.controller.response.WeeklyReportResponse;

public interface FindWeeklyReportService {

    WeeklyReportResponse findWeeklyReport(Long requesterMemberId, Long subId, Long reportId);
}
