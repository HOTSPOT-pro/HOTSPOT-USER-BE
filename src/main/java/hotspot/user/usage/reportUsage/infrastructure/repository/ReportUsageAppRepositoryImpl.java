package hotspot.user.usage.reportUsage.infrastructure.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import hotspot.user.usage.reportUsage.domain.AppUsage;
import hotspot.user.usage.reportUsage.service.port.ReportUsageAppRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReportUsageAppRepositoryImpl implements ReportUsageAppRepository {

    private final ReportUsageAppRedisRepository redisRepository;

    @Override
    public List<AppUsage> findMonthlyAppUsage(Long subId) {
        return redisRepository.findMonthlyAppUsage(subId);
    }

    @Override
    public List<AppUsage> findDailyAppUsage(Long subId, LocalDate date) {
        return redisRepository.findDailyAppUsage(subId, date);
    }
}
