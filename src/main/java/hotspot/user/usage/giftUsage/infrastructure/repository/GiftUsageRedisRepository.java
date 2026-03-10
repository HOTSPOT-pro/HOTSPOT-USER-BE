package hotspot.user.usage.giftUsage.infrastructure.repository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import hotspot.user.common.util.redis.PipelineResultMapper;
import hotspot.user.common.util.redis.RedisPipelineExecutor;
import hotspot.user.common.util.redis.RedisValueParser;
import hotspot.user.usage.giftUsage.domain.GiftUsage;
import hotspot.user.usage.giftUsage.infrastructure.keybuilder.GiftUsageRedisKeyBuilder;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GiftUsageRedisRepository {

    private static final String K_GIFT_LIMIT_PREFIX = "gift_limit:";
    private static final String K_GIFT_USED_PREFIX = "gift_used:";

    private final RedisPipelineExecutor pipelineExecutor;
    private final StringRedisTemplate redisTemplate;
    private final Clock clock;

    public List<GiftUsage> findGiftUsageList(Long subId) {

        LocalDate now = LocalDate.now(clock);

        List<String> giftIds = new ArrayList<>(
                Objects.requireNonNull(redisTemplate.opsForZSet()
                        .range(
                                GiftUsageRedisKeyBuilder.giftIndex(subId, now),
                                0,
                                -1
                        ))
        );

        if (giftIds.isEmpty()) {
            return List.of();
        }

        PipelineResult pipeline =
                executeGiftOnlyPipeline(subId, giftIds, now);

        Map<String, Object> resultMap =
                PipelineResultMapper.toMap(
                        pipeline.requestKeys(),
                        pipeline.rawResults()
                );

        return buildGiftUsageList(giftIds, resultMap);
    }

    private PipelineResult executeGiftOnlyPipeline(
            Long subId,
            List<String> giftIds,
            LocalDate now
    ) {

        List<String> requestKeys = new ArrayList<>();

        List<Object> rawResults =
                pipelineExecutor.execute((RedisCallback<Object>) connection -> {

                    addGiftRequests(connection, subId, giftIds, requestKeys, now);

                    return null;
                });

        return new PipelineResult(requestKeys, rawResults);
    }

    private void addGiftRequests(
            RedisConnection connection,
            Long subId,
            List<String> giftIds,
            List<String> requestKeys,
            LocalDate now
    ) {

        for (String giftIdStr : giftIds) {

            Long giftId = Long.parseLong(giftIdStr);

            requestKeys.add(K_GIFT_LIMIT_PREFIX + giftId);

            connection.hGet(
                    pipelineExecutor.serialize(
                            GiftUsageRedisKeyBuilder.giftLimit(subId, giftId, now)
                    ),
                    pipelineExecutor.serialize("gift_limit")
            );

            requestKeys.add(K_GIFT_USED_PREFIX + giftId);

            connection.hGet(
                    pipelineExecutor.serialize(
                            GiftUsageRedisKeyBuilder.giftUsage(subId, giftId, now)
                    ),
                    pipelineExecutor.serialize("gift_used")
            );
        }
    }

    private List<GiftUsage> buildGiftUsageList(
            List<String> giftIds,
            Map<String, Object> resultMap
    ) {

        List<GiftUsage> gifts = new ArrayList<>();

        for (String giftIdStr : giftIds) {

            Long giftId = Long.parseLong(giftIdStr);

            double limitKb =
                    resultMap.get(K_GIFT_LIMIT_PREFIX + giftId) == null
                            ? 0D
                            : RedisValueParser.toDouble(
                            resultMap.get(K_GIFT_LIMIT_PREFIX + giftId)
                    );

            double usedKb =
                    resultMap.get(K_GIFT_USED_PREFIX + giftId) == null
                            ? 0D
                            : RedisValueParser.toDouble(
                            resultMap.get(K_GIFT_USED_PREFIX + giftId)
                    );

            gifts.add(new GiftUsage(giftId, limitKb, usedKb));
        }

        return gifts;
    }

    private record PipelineResult(
            List<String> requestKeys,
            List<Object> rawResults
    ) {}
}
