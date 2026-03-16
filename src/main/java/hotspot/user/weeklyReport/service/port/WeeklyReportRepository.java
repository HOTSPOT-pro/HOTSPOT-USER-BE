package hotspot.user.weeklyReport.service.port;

import java.util.Optional;

import hotspot.user.weeklyReport.domain.WeeklyReport;

public interface WeeklyReportRepository {
    Optional<WeeklyReport> findByReportId(Long reportId);
}
