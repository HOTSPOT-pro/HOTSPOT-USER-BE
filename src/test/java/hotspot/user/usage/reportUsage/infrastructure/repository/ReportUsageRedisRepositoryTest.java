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
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import hotspot.user.common.config.RedisLuaConfig;
import hotspot.user.usage.reportUsage.infrastructure.keybuilder.ReportUsageRedisKeyBuilder;

@Testcontainers
@DataRedisTest
@Import({
        ReportUsageRedisRepository.class,
        RedisLuaConfig.class
})
class ReportUsageRedisRepositoryTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestBootConfig {}

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port",
                () -> redis.getMappedPort(6379));
    }

    @Autowired
    ReportUsageRedisRepository repository;

    @Autowired
    StringRedisTemplate redisTemplate;

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
