package hotspot.user.weeklyReport.service.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import hotspot.user.weeklyReport.domain.WeeklyReport;

public interface WeeklyReportRepository {
    Optional<WeeklyReport> findByReportId(Long reportId);
    Map<Long, Long> findCompletedCurrentWeekReportIdsBySubIds(
            List<Long> subIds,
            LocalDate currentWeekStartDate,
            LocalDate currentWeekEndDate
    );
}
