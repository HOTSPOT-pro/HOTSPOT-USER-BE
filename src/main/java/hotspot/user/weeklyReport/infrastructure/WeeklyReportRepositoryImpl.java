package hotspot.user.weeklyReport.infrastructure;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public List<WeeklyReport> findMonthlyReportsBySubId(Long subId, YearMonth yearMonth) {
        LocalDate monthStartDate = yearMonth.atDay(1);
        LocalDate monthEndDate = yearMonth.atEndOfMonth();

        return weeklyReportJpaRepository.findMonthlyReportsBySubId(
                        subId,
                        monthStartDate.minusDays(1),
                        monthEndDate.minusDays(1)
                )
                .stream()
                .map(WeeklyReportEntity::entityToDomain)
                .toList();
    }

    @Override
    public Map<Long, Long> findCompletedCurrentWeekReportIdsBySubIds(
            List<Long> subIds,
            LocalDate currentWeekStartDate,
            LocalDate currentWeekEndDate
    ) {
        if (subIds == null || subIds.isEmpty()) {
            return Map.of();
        }

        return weeklyReportJpaRepository.findCompletedCurrentWeekReportIdsBySubIds(
                        subIds,
                        currentWeekStartDate.minusDays(1),
                        currentWeekEndDate.minusDays(1)
                )
                .stream()
                .collect(Collectors.toMap(
                        WeeklyReportJpaRepository.WeeklyReportIdRow::getSubId,
                        WeeklyReportJpaRepository.WeeklyReportIdRow::getWeeklyReportId,
                        (left, right) -> left
                ));
    }
}
