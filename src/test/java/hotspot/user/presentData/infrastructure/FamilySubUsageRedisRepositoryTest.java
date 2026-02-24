package hotspot.user.presentData.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

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

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.presentData.domain.SubUsage;
import hotspot.user.presentData.infrastructure.mapper.FamilySubUsageMapper;

@Testcontainers
@DataRedisTest
@Import({
        FamilySubUsageRedisRepository.class,
        FamilySubUsageRedisRepositoryTest.RedisTestConfig.class
})
class FamilySubUsageRedisRepositoryTest {

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
    FamilySubUsageRedisRepository repository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @TestConfiguration
    static class RedisTestConfig {

        @Bean
        FamilySubUsageMapper familySubUsageMapper() {
            return new FamilySubUsageMapper();
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
    @DisplayName("sub별 사용량(plan_used) + 한도(plan_limit) pipelined 조회 성공 (DAY/MONTH 혼합)")
    void shouldReturnUsageAndLimitSuccessfully() {

        LocalDate now = LocalDate.now();
        String yyyyMM = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        String yyyyMMdd = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        // given: subPeriodMap (순서 유지 위해 LinkedHashMap)
        Map<Long, DataPeriod> subPeriodMap = new LinkedHashMap<>();
        subPeriodMap.put(1L, DataPeriod.MONTH); // 월 요금제 → usage:sub:1:yyyyMM
        subPeriodMap.put(2L, DataPeriod.DAY);   // 일 요금제 → usage:sub:2:yyyyMMdd

        // Redis 저장 단위는 KB
        // sub1: used=3GB, limit=10GB
        redisTemplate.opsForHash().put(
                "usage:sub:1:" + yyyyMM,
                "plan_used",
                String.valueOf(3L * 1024 * 1024) // 3145728 KB
        );
        redisTemplate.opsForHash().put(
                "limit:sub:1",
                "plan_limit",
                String.valueOf(10L * 1024 * 1024) // 10485760 KB
        );

        // sub2: used=2GB, limit=5GB
        redisTemplate.opsForHash().put(
                "usage:sub:2:" + yyyyMMdd,
                "plan_used",
                String.valueOf(2L * 1024 * 1024) // 2097152 KB
        );
        redisTemplate.opsForHash().put(
                "limit:sub:2",
                "plan_limit",
                String.valueOf(5L * 1024 * 1024) // 5242880 KB
        );

        // when
        Map<Long, SubUsage> result = repository.findUsageAndLimit(subPeriodMap);

        // then
        assertThat(result).containsKeys(1L, 2L);

        SubUsage sub1 = result.get(1L);
        assertThat(sub1.usedGb()).isEqualTo(3.0);
        assertThat(sub1.limitGb()).isEqualTo(10.0);
        assertThat(sub1.remainGb()).isEqualTo(7.0);
        assertThat(sub1.percent()).isEqualTo(30);

        SubUsage sub2 = result.get(2L);
        assertThat(sub2.usedGb()).isEqualTo(2.0);
        assertThat(sub2.limitGb()).isEqualTo(5.0);
        assertThat(sub2.remainGb()).isEqualTo(3.0);
        assertThat(sub2.percent()).isEqualTo(40);
    }

    @Test
    @DisplayName("Redis에 값이 없으면 0으로 처리")
    void shouldReturnZeroWhenMissing() {

        Map<Long, DataPeriod> subPeriodMap = new LinkedHashMap<>();
        subPeriodMap.put(1L, DataPeriod.MONTH);

        Map<Long, SubUsage> result = repository.findUsageAndLimit(subPeriodMap);

        SubUsage sub1 = result.get(1L);
        assertThat(sub1.usedGb()).isEqualTo(0.0);
        assertThat(sub1.limitGb()).isEqualTo(0.0);
        assertThat(sub1.remainGb()).isEqualTo(0.0);
        assertThat(sub1.percent()).isEqualTo(0);
    }

    @Test
    @DisplayName("무제한(-1) 한도는 -1GB로 내려가도록(현재 구현 그대로면 0.0이 나올 수 있음) - 정책에 맞게 조정 필요")
    void unlimitedPlanLimitShouldBeHandled() {

        LocalDate now = LocalDate.now();
        String yyyyMM = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));

        Map<Long, DataPeriod> subPeriodMap = new LinkedHashMap<>();
        subPeriodMap.put(2L, DataPeriod.MONTH);

        // used=0, limit=-1
        redisTemplate.opsForHash().put(
                "usage:sub:2:" + yyyyMM,
                "plan_used",
                "0"
        );
        redisTemplate.opsForHash().put(
                "limit:sub:2",
                "plan_limit",
                "-1"
        );

        Map<Long, SubUsage> result = repository.findUsageAndLimit(subPeriodMap);
        SubUsage sub2 = result.get(2L);

        // 현재 Calculator 정책(음수 clamp) 기준
        assertThat(sub2.limitGb()).isEqualTo(-1.0);
        assertThat(sub2.usedGb()).isEqualTo(0.0);
        assertThat(sub2.percent()).isEqualTo(0);
    }
}
