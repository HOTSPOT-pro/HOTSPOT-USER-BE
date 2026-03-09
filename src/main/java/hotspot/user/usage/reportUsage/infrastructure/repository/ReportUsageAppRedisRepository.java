package hotspot.user.usage.reportUsage.infrastructure.repository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import hotspot.user.common.util.redis.RedisUsageCalculator;
import hotspot.user.common.util.redis.RedisValueParser;
import hotspot.user.usage.reportUsage.domain.AppUsage;
import hotspot.user.usage.reportUsage.infrastructure.keybuilder.ReportUsageRedisKeyBuilder;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReportUsageAppRedisRepository {

    private final StringRedisTemplate redisTemplate;
    private final Clock clock;

    public List<AppUsage> findMonthlyAppUsage(Long subId) {

        LocalDate now = LocalDate.now(clock);

        String key =
                ReportUsageRedisKeyBuilder
                        .monthlyAppUsage(subId, now);

        return getAppUsageFromZSet(key);
    }

    public List<AppUsage> findDailyAppUsage(Long subId, LocalDate date) {

        String key =
                ReportUsageRedisKeyBuilder
                        .dailyAppUsage(subId, date);

        return getAppUsageFromZSet(key);
    }

    private List<AppUsage> getAppUsageFromZSet(String key) {

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet()
                        .reverseRangeWithScores(key, 0, -1);

        if (tuples == null || tuples.isEmpty()) {
            return Collections.emptyList();
        }

        return tuples.stream()
                .filter(tuple -> tuple.getValue() != null)
                .map(tuple -> {

                    Long appId = RedisValueParser.toLong(tuple.getValue());


                    double usedKb =
                            tuple.getScore() == null
                                    ? 0D
                                    : tuple.getScore();

                    double usedGb =
                            RedisUsageCalculator.kbToGb(usedKb);

                    return new AppUsage(appId, usedGb);
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
