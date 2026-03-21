package hotspot.user.usage.giftUsage.infrastructure.repository;

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
import hotspot.user.usage.giftUsage.domain.GiftUsage;

@Import({
        GiftUsageRedisRepository.class,
        GiftUsageRedisRepositoryTest.RedisTestConfig.class
})
class GiftUsageRedisRepositoryTest extends AbstractRedisTest {

    @Autowired
    GiftUsageRedisRepository repository;

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
    @DisplayName("선물 사용량 조회 성공")
    void shouldReturnGiftUsageList() {

        String yyyyMM = "202602";

        redisTemplate.opsForZSet()
                .add("idx:gift:1:" + yyyyMM, "101", 1);

        redisTemplate.opsForHash().put(
                "limit:gift:1:101:" + yyyyMM,
                "gift_limit",
                "5242880"
        );

        redisTemplate.opsForHash().put(
                "usage:gift:1:101:" + yyyyMM,
                "gift_used",
                "1048576"
        );

        List<GiftUsage> result =
                repository.findGiftUsageList(1L);

        assertEquals(1, result.size());

        GiftUsage gift = result.get(0);

        assertEquals(101L, gift.giftId());
        assertEquals(5.0, gift.limitGb());
        assertEquals(1.0, gift.usedGb());
        assertEquals(4.0, gift.remainGb());
    }

    @Test
    @DisplayName("선물이 없으면 빈 리스트 반환")
    void shouldReturnEmptyListWhenNoGift() {

        List<GiftUsage> result =
                repository.findGiftUsageList(1L);

        assertEquals(0, result.size());
    }
}
