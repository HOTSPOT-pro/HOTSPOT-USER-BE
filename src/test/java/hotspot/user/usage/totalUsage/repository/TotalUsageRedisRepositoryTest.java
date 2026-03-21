package hotspot.user.usage.totalUsage.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import hotspot.user.common.config.AbstractRedisTest;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.util.redis.RedisPipelineExecutor;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.usage.totalUsage.repository.schema.TotalUsage;

@Import({
        TotalUsageRedisRepository.class,
        TotalUsageRedisRepositoryTest.RedisTestConfig.class
})
class TotalUsageRedisRepositoryTest extends AbstractRedisTest {

    @Autowired
    TotalUsageRedisRepository repository;

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
                    LocalDate.of(2026, 3, 10)
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
    @DisplayName("MONTH 요금제 전체 사용량 조회 성공")
    void shouldReturnTotalUsageForMonth() {

        String yyyyMM = "202603";

        // 개인 한도 20GB
        redisTemplate.opsForHash().put(
                "limit:sub:7",
                "plan_limit",
                "20971520"
        );

        // 개인 사용량 3GB
        redisTemplate.opsForHash().put(
                "usage:sub:7:" + yyyyMM,
                "plan_used",
                "3145728"
        );

        // 가족 사용량 2GB
        redisTemplate.opsForHash().put(
                "usage:sub:7:" + yyyyMM,
                "member_family_used",
                "2097152"
        );

        // 가족 한도 10GB
        redisTemplate.opsForHash().put(
                "limit:family_sub:2:7",
                "family_limit",
                "10485760"
        );

        // 선물 index
        redisTemplate.opsForZSet().add(
                "idx:gift:7:" + yyyyMM,
                "501",
                1
        );
        redisTemplate.opsForZSet().add(
                "idx:gift:7:" + yyyyMM,
                "502",
                2
        );

        // 선물1: 5GB, 사용 1GB
        redisTemplate.opsForHash().put(
                "limit:gift:7:501:" + yyyyMM,
                "gift_limit",
                "5242880"
        );
        redisTemplate.opsForHash().put(
                "usage:gift:7:501:" + yyyyMM,
                "gift_used",
                "1048576"
        );

        // 선물2: 3GB, 사용 1GB
        redisTemplate.opsForHash().put(
                "limit:gift:7:502:" + yyyyMM,
                "gift_limit",
                "3145728"
        );
        redisTemplate.opsForHash().put(
                "usage:gift:7:502:" + yyyyMM,
                "gift_used",
                "1048576"
        );

        TotalUsage result =
                repository.findTotalUsage(7L, 2L, DataPeriod.MONTH);

        // 총 한도 = 20 + 10 + 5 + 3 = 38
        assertEquals(38.0, result.totalDataAmount());

        // 총 잔여 = (20-3) + (10-2) + (5-1) + (3-1) = 31
        assertEquals(31.0, result.totalDataRemainAmount());

        // 31 / 38 * 100 = 81.57 -> 82
        assertEquals(82, result.totalDataRemainPercent());

        // 개인 잔여 = 17
        assertEquals(17.0, result.subDataRemainAmount());

        // 선물 잔여 = 4 + 2 = 6
        assertEquals(6.0, result.giftDataRemainAmount());

        // 가족 잔여 = 8
        assertEquals(8.0, result.familyDataRemainAmount());
    }

    @Test
    @DisplayName("DAY 요금제 전체 사용량 조회 성공")
    void shouldReturnTotalUsageForDay() {

        String yyyyMMdd = "20260310";
        String yyyyMM = "202603";

        // 개인 한도 5GB
        redisTemplate.opsForHash().put(
                "limit:sub:7",
                "plan_limit",
                "5242880"
        );

        // 개인 사용량 1GB
        redisTemplate.opsForHash().put(
                "usage:sub:7:" + yyyyMMdd,
                "plan_used",
                "1048576"
        );

        // 가족 사용량 1GB
        redisTemplate.opsForHash().put(
                "usage:sub:7:" + yyyyMMdd,
                "member_family_used",
                "1048576"
        );

        // 가족 한도 4GB
        redisTemplate.opsForHash().put(
                "limit:family_sub:2:7",
                "family_limit",
                "4194304"
        );

        // 선물 index
        redisTemplate.opsForZSet().add(
                "idx:gift:7:" + yyyyMM,
                "601",
                1
        );

        // 선물1: 2GB, 사용 1GB
        redisTemplate.opsForHash().put(
                "limit:gift:7:601:" + yyyyMM,
                "gift_limit",
                "2097152"
        );
        redisTemplate.opsForHash().put(
                "usage:gift:7:601:" + yyyyMM,
                "gift_used",
                "1048576"
        );

        TotalUsage result =
                repository.findTotalUsage(7L, 2L, DataPeriod.DAY);

        // 총 한도 = 5 + 4 + 2 = 11
        assertEquals(11.0, result.totalDataAmount());

        // 총 잔여 = (5-1) + (4-1) + (2-1) = 8
        assertEquals(8.0, result.totalDataRemainAmount());

        // 8 / 11 * 100 = 72.72 -> 73
        assertEquals(73, result.totalDataRemainPercent());

        assertEquals(4.0, result.subDataRemainAmount());
        assertEquals(1.0, result.giftDataRemainAmount());
        assertEquals(3.0, result.familyDataRemainAmount());
    }

    @Test
    @DisplayName("plan_limit 없으면 예외 발생")
    void shouldThrowExceptionWhenPlanLimitMissing() {

        redisTemplate.opsForHash().put(
                "limit:family_sub:2:7",
                "family_limit",
                "10485760"
        );

        assertThrows(
                ApplicationException.class,
                () -> repository.findTotalUsage(7L, 2L, DataPeriod.MONTH)
        );
    }

    @Test
    @DisplayName("family_limit 없으면 예외 발생")
    void shouldThrowExceptionWhenFamilyLimitMissing() {

        redisTemplate.opsForHash().put(
                "limit:sub:7",
                "plan_limit",
                "20971520"
        );

        assertThrows(
                ApplicationException.class,
                () -> repository.findTotalUsage(7L, 2L, DataPeriod.MONTH)
        );
    }
}
