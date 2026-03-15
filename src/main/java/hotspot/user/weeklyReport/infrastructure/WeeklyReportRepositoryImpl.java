package hotspot.user.weeklyReport.infrastructure;

import hotspot.user.weeklyReport.domain.WeeklyReport;
import hotspot.user.weeklyReport.infrastructure.entity.WeeklyReportEntity;
import hotspot.user.weeklyReport.service.port.WeeklyReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WeeklyReportRepositoryImpl implements WeeklyReportRepository {

    private final WeeklyReportJpaRepository weeklyReportJpaRepository;

    @Override
    public Optional<WeeklyReport> findByReportId(Long reportId) {

        return weeklyReportJpaRepository.findByWeeklyReportId(reportId)
                .map(WeeklyReportEntity::entityToDomain)
                .orElseThrow(null);
    }
}
