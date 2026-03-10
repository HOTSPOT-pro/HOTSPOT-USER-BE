package hotspot.user.usage.subscriptionUsage.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;

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

import hotspot.user.common.util.redis.RedisPipelineExecutor;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;

@Testcontainers
@DataRedisTest
@Import({
        SubscriptionUsageRedisRepository.class,
        SubscriptionUsageRedisRepositoryTest.RedisTestConfig.class
})
class SubscriptionUsageRedisRepositoryTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestBootConfig {}

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
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    SubscriptionUsageRedisRepository repository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @TestConfiguration
    static class RedisTestConfig {

        @Bean
        RedisPipelineExecutor redisPipelineExecutor(StringRedisTemplate redisTemplate) {
            return new RedisPipelineExecutor(redisTemplate);
        }

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
    @DisplayName("findSubscriptionUsage(MONTH): 개인 데이터 조회")
    void shouldReturnSubscriptionUsageSuccessfully() {

        String yyyyMM = "202602";

        // 개인 한도 24GB
        redisTemplate.opsForHash().put(
                "limit:sub:1",
                "plan_limit",
                "25165824"
        );

        // 개인 사용량 3GB
        redisTemplate.opsForHash().put(
                "usage:sub:1:" + yyyyMM,
                "plan_used",
                "3145728"
        );

        SubscriptionUsage usage =
                repository.findSubscriptionUsage(1L, DataPeriod.MONTH);

        assertEquals(24.0, usage.limitGb());
        assertEquals(3.0, usage.usedGb());
        assertEquals(21.0, usage.remainGb());
        assertEquals(88, usage.remainPercent());
    }

    @Test
    @DisplayName("findSubscriptionUsage(DAY): 일 기준 사용량 조회")
    void shouldReturnSubscriptionUsageForDay() {

        String yyyyMMdd = "20260201";

        redisTemplate.opsForHash().put(
                "limit:sub:1",
                "plan_limit",
                "5242880"
        );

        redisTemplate.opsForHash().put(
                "usage:sub:1:" + yyyyMMdd,
                "plan_used",
                "1048576"
        );

        SubscriptionUsage usage =
                repository.findSubscriptionUsage(1L, DataPeriod.DAY);

        assertEquals(5.0, usage.limitGb());
        assertEquals(1.0, usage.usedGb());
        assertEquals(4.0, usage.remainGb());
    }

    @Test
    @DisplayName("findRemainingPlanKb(MONTH): plan_limit - plan_used 계산")
    void shouldReturnRemainingPlanKbForMonth() {

        String yyyyMM = "202602";

        redisTemplate.opsForHash().put(
                "limit:sub:1",
                "plan_limit",
                "25165824"
        );

        redisTemplate.opsForHash().put(
                "usage:sub:1:" + yyyyMM,
                "plan_used",
                "3145728"
        );

        long remainingKb =
                repository.findRemainingPlanKb(1L, DataPeriod.MONTH);

        assertEquals(22020096L, remainingKb);
    }

    @Test
    @DisplayName("findRemainingPlanKb(DAY): plan_limit - plan_used 계산")
    void shouldReturnRemainingPlanKbForDay() {

        String yyyyMMdd = "20260201";

        redisTemplate.opsForHash().put(
                "limit:sub:1",
                "plan_limit",
                "5242880"
        );

        redisTemplate.opsForHash().put(
                "usage:sub:1:" + yyyyMMdd,
                "plan_used",
                "1048576"
        );

        long remainingKb =
                repository.findRemainingPlanKb(1L, DataPeriod.DAY);

        assertEquals(4194304L, remainingKb);
    }

    @Test
    @DisplayName("findRemainingPlanKb: plan_used가 없으면 0으로 처리")
    void shouldTreatMissingPlanUsedAsZero() {

        String yyyyMM = "202602";

        redisTemplate.opsForHash().put(
                "limit:sub:1",
                "plan_limit",
                "1048576"
        );

        // usage는 있지만 plan_used 없음
        redisTemplate.opsForHash().put(
                "usage:sub:1:" + yyyyMM,
                "member_family_used",
                "999"
        );

        long remainingKb =
                repository.findRemainingPlanKb(1L, DataPeriod.MONTH);

        assertEquals(1048576L, remainingKb);
    }
}