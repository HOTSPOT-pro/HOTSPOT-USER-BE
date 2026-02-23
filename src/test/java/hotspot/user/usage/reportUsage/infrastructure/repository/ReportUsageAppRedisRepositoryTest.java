package hotspot.user.usage.reportUsage.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import hotspot.user.usage.reportUsage.domain.AppUsage;
import hotspot.user.usage.reportUsage.infrastructure.keybuilder.ReportUsageRedisKeyBuilder;

@Testcontainers
@DataRedisTest
@Import({
        ReportUsageAppRedisRepository.class,
        ReportUsageAppRedisRepositoryTest.RedisTestConfig.class
})
class ReportUsageAppRedisRepositoryTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestBootConfig {
    }

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379)
                    .waitingFor(
                            Wait.forListeningPort()
                                    .withStartupTimeout(Duration.ofSeconds(30))
                    );

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port",
                () -> redis.getMappedPort(6379));
    }

    @Autowired
    ReportUsageAppRedisRepository repository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @TestConfiguration
    static class RedisTestConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(
                    LocalDate.of(2026, 2, 1)
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant(),
                    ZoneId.systemDefault()
            );
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
    @DisplayName("월별 앱 사용량 정상 조회 (ZSet → AppUsage 변환)")
    void shouldReturnMonthlyAppUsage() {

        Long subId = 1L;
        LocalDate now = LocalDate.of(2026, 2, 1);

        String key =
                ReportUsageRedisKeyBuilder
                        .monthlyAppUsage(subId, now);

        // appId=100 → 2GB (KB 단위로 저장)
        double usedKb = 2 * 1024 * 1024; // 2GB
        redisTemplate.opsForZSet()
                .add(key, "100", usedKb);

        List<AppUsage> result =
                repository.findMonthlyAppUsage(subId);

        assertEquals(1, result.size());

        AppUsage usage = result.get(0);
        assertEquals(100L, usage.getAppId());
        assertEquals(2.0, usage.getUsedGb());
    }

    @Test
    @DisplayName("ZSet이 비어있으면 빈 리스트 반환")
    void shouldReturnEmptyListWhenNoData() {

        List<AppUsage> result =
                repository.findDailyAppUsage(1L);

        assertTrue(result.isEmpty());
    }
}
