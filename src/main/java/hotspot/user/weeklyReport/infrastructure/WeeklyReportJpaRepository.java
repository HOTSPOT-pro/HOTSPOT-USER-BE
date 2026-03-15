package hotspot.user.weeklyReport.infrastructure;

import hotspot.user.weeklyReport.infrastructure.entity.WeeklyReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeeklyReportJpaRepository extends JpaRepository<WeeklyReportEntity, Long> {
    Optional<WeeklyReportEntity> findByWeeklyReportId(Long reportId);
}
