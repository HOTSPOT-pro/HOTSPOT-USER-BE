package hotspot.user.weeklyReport.domain.mapper;

import hotspot.user.weeklyReport.controller.response.WeeklyReportResponse;
import hotspot.user.weeklyReport.domain.WeeklyReport;

public class WeeklyReportMapper {
    public static WeeklyReportResponse toWeeklyReportResponse(WeeklyReport weeklyReport) {
        return WeeklyReportResponse.builder().build();
    }
}
