package hotspot.user.weeklyReport.service.port;

public interface WeeklyReportRepository {
    WeeklyReport findByReportId(Long reportId);
}
