package hotspot.user.usage.reportUsage.infrastructure.repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import hotspot.user.common.util.UsageCalculator;
import hotspot.user.usage.reportUsage.infrastructure.keybuilder.ReportUsageRedisKeyBuilder;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReportUsageRedisRepository {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> usageSumScript;


    public Map<Long, Map<LocalDate, Double>> findReportUsageDailyGb(
            List<Long> subIds,
            List<LocalDate> dates
    ) {

        List<String> keys = new ArrayList<>();

        for (Long subId : subIds) {
            for (LocalDate date : dates) {

                keys.add(
                        ReportUsageRedisKeyBuilder
                                .dailyAppUsage(subId, date)
                );
            }
        }

        List<Long> rawSums =
                redisTemplate.execute(usageSumScript, keys);

        Map<Long, Map<LocalDate, Double>> result =
                new LinkedHashMap<>();

        int index = 0;

        for (Long subId : subIds) {
            for (LocalDate date : dates) {

                long totalKb = 0L;

                if (index < rawSums.size() && rawSums.get(index) != null) {
                    totalKb = rawSums.get(index);
                }

                index++;

                double gb =
                        UsageCalculator.kbToGb(totalKb);

                result
                        .computeIfAbsent(subId, k -> new LinkedHashMap<>())
                        .put(date, gb);
            }
        }

        return result;
    }

    public Map<Long, Map<YearMonth, Double>> findReportUsageMonthlyGb(
            List<Long> subIds,
            List<YearMonth> months
    ) {

        List<String> keys = new ArrayList<>();

        for (Long subId : subIds) {
            for (YearMonth month : months) {

                keys.add(
                        ReportUsageRedisKeyBuilder
                                .monthlyAppUsage(subId, month.atDay(1))
                );
            }
        }

        List<Long> rawSums =
                redisTemplate.execute(usageSumScript, keys);

        Map<Long, Map<YearMonth, Double>> result =
                new LinkedHashMap<>();

        int index = 0;

        for (Long subId : subIds) {
            for (YearMonth month : months) {

                long totalKb = 0L;

                if (index < rawSums.size() && rawSums.get(index) != null) {
                    totalKb = rawSums.get(index);
                }

                index++;

                double gb =
                        UsageCalculator.kbToGb(totalKb);

                result
                        .computeIfAbsent(subId, k -> new LinkedHashMap<>())
                        .put(month, gb);
            }
        }

        return result;
    }
}
