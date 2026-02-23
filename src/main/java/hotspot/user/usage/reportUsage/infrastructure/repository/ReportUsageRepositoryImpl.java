package hotspot.user.usage.reportUsage.infrastructure.repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import hotspot.user.usage.reportUsage.service.port.ReportUsageRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReportUsageRepositoryImpl implements ReportUsageRepository {

    private final ReportUsageRedisRepository redisRepository;

    @Override
    public Map<Long, Map<LocalDate, Double>> findReportUsageDailyGb(
            List<Long> subIds,
            List<LocalDate> dates
    ) {
        return redisRepository.findReportUsageDailyGb(subIds, dates);
    }

    @Override
    public Map<Long, Map<YearMonth, Double>> findReportUsageMonthlyGb(
            List<Long> subIds,
            List<YearMonth> months
    ) {
        return redisRepository.findReportUsageMonthlyGb(subIds, months);
    }
}
