package hotspot.user.weeklyReport.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.weeklyReport.infrastructure.entity.WeeklyReportEntity;

public interface WeeklyReportJpaRepository extends JpaRepository<WeeklyReportEntity, Long> {
}
