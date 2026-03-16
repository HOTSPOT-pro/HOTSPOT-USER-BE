package hotspot.user.usage.familyUsage.infrastructure.respository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import hotspot.user.common.config.AbstractRedisTest;
import hotspot.user.common.util.redis.RedisPipelineExecutor;
import hotspot.user.usage.familyUsage.domain.FamilyUsage;

@Import({
        FamilyUsageRedisRepository.class,
        FamilyUsageRedisRepositoryTest.RedisTestConfig.class
})
class FamilyUsageRedisRepositoryTest extends AbstractRedisTest {

    @Autowired
    FamilyUsageRedisRepository repository;

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
    @DisplayName("가족 사용량 정상 조회")
    void shouldReturnFamilyUsageSuccessfully() {

        String yyyyMM = "202602";

        // 가족 한도 20GB
        redisTemplate.opsForHash().put(
                "limit:family:1",
                "family_limit",
                "20971520"
        );

        // 가족 사용량 5GB
        redisTemplate.opsForHash().put(
                "usage:family:1:" + yyyyMM,
                "family_used",
                "5242880"
        );

        // 구성원 1 한도 10GB
        redisTemplate.opsForHash().put(
                "limit:family_sub:1:1",
                "family_limit",
                "10485760"
        );

        // 구성원 1 사용량 3GB
        redisTemplate.opsForHash().put(
                "usage:sub:1:" + yyyyMM,
                "member_family_used",
                "3145728"
        );

        FamilyUsage usage =
                repository.findFamilyAndSubData(1L, List.of(1L));

        assertEquals(20.0, usage.familyLimitGb());
        assertEquals(5.0, usage.familyUsedGb());
        assertEquals(3.0,
                usage.getSubOrZero(1L).familyUsedGb());
    }
}
