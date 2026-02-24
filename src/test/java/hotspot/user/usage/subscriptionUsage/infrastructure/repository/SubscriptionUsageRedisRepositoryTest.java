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
        registry.add("spring.data.redis.port",
                () -> redis.getMappedPort(6379));
    }

    @Autowired
    SubscriptionUsageRedisRepository repository;

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
    @DisplayName("MONTH 요금제 개인 + 선물 사용량 정상 조회")
    void shouldReturnSubscriptionUsageSuccessfully() {

        String yyyyMM = "202602";

        // 개인 한도 24GB (KB)
        redisTemplate.opsForHash().put(
                "limit:sub:1",
                "plan_limit",
                "25165824"
        );

        // 🔥 개인 사용량 (member_family_used로 변경)
        redisTemplate.opsForHash().put(
                "usage:sub:1:" + yyyyMM,
                "member_family_used",
                "0"
        );

        // 선물 index
        redisTemplate.opsForZSet().add(
                "idx:gift:1:" + yyyyMM,
                "69395",
                1
        );

        // 선물 한도 1GB
        redisTemplate.opsForHash().put(
                "limit:gift:1:69395:" + yyyyMM,
                "gift_limit",
                "1048576"
        );

        // 선물 사용량 0.5GB
        redisTemplate.opsForHash().put(
                "usage:gift:1:69395:" + yyyyMM,
                "gift_used",
                "524288"
        );

        SubscriptionUsage usage =
                repository.findSubscriptionUsage(
                        1L,
                        DataPeriod.MONTH
                );

        assertEquals(24.0, usage.limitGb());
        assertEquals(1.0, usage.giftTotalLimitGb());
        assertEquals(0.5, usage.giftTotalUsedGb());
    }
}
