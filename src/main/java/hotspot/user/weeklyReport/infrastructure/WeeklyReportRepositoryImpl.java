package hotspot.user.weeklyReport.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.weeklyReport.domain.WeeklyReport;
import hotspot.user.weeklyReport.infrastructure.entity.WeeklyReportEntity;
import hotspot.user.weeklyReport.service.port.WeeklyReportRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WeeklyReportRepositoryImpl implements WeeklyReportRepository {

    private final WeeklyReportJpaRepository weeklyReportJpaRepository;

    @Override
    public Optional<WeeklyReport> findByReportId(Long reportId) {
        return weeklyReportJpaRepository.findById(reportId)
                .map(WeeklyReportEntity::entityToDomain);
    }
}
