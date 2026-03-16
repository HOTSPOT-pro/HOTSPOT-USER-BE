package hotspot.user.usage.reportUsage.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import hotspot.user.common.config.AbstractRedisTest;
import hotspot.user.common.config.RedisLuaConfig;
import hotspot.user.common.util.redis.RedisPipelineExecutor;
import hotspot.user.usage.reportUsage.infrastructure.keybuilder.ReportUsageRedisKeyBuilder;

@Import({
        ReportUsageRedisRepository.class,
        RedisLuaConfig.class
})
class ReportUsageRedisRepositoryTest extends AbstractRedisTest {

    @Autowired
    ReportUsageRedisRepository repository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @TestConfiguration
    static class RedisTestConfig {

        @Bean
        RedisPipelineExecutor redisPipelineExecutor(
                StringRedisTemplate redisTemplate
        ) {
            return new RedisPipelineExecutor(redisTemplate);
        }
    }

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    @Test
    void shouldReturnDailyUsageCorrectly() {

        Long subId = 1L;
        LocalDate date = LocalDate.of(2026, 2, 1);

        String key =
                ReportUsageRedisKeyBuilder.dailyAppUsage(subId, date);

        long kb = 2L * 1024 * 1024;

        redisTemplate.opsForZSet()
                .add(key, "1", kb);

        Map<Long, Map<LocalDate, Double>> result =
                repository.findReportUsageDailyGb(
                        List.of(subId),
                        List.of(date)
                );

        assertEquals(2.0,
                result.get(subId).get(date));
    }

    @Test
    @DisplayName("월별 사용량 Lua Script 합산 테스트")
    void shouldReturnMonthlyUsageCorrectly() {

        Long subId = 1L;
        YearMonth month = YearMonth.of(2026, 2);

        String key =
                ReportUsageRedisKeyBuilder.monthlyAppUsage(
                        subId,
                        month.atDay(1)
                );

        long kb = 5L * 1024 * 1024;

        redisTemplate.opsForZSet()
                .add(key, "1", kb);   // 👈 STRING 말고 ZSET

        Map<Long, Map<YearMonth, Double>> result =
                repository.findReportUsageMonthlyGb(
                        List.of(subId),
                        List.of(month)
                );

        assertEquals(5.0,
                result.get(subId).get(month));
    }
}
