package hotspot.user.usage.subscriptionUsage.infrastructure.repository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SubscriptionUsageErrorCode;
import hotspot.user.common.util.redis.PipelineResultMapper;
import hotspot.user.common.util.redis.RedisPipelineExecutor;
import hotspot.user.common.util.redis.RedisValueParser;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.usage.subscriptionUsage.domain.GiftUsage;
import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;
import hotspot.user.usage.subscriptionUsage.infrastructure.keybuilder.SubscriptionUsageRedisKeyBuilder;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubscriptionUsageRedisRepository {

    private static final String K_PLAN_LIMIT = "plan_limit";
    private static final String K_PLAN_USED  = "plan_used";
    private static final String K_GIFT_LIMIT_PREFIX = "gift_limit:";
    private static final String K_GIFT_USED_PREFIX  = "gift_used:";

    private final RedisPipelineExecutor pipelineExecutor;
    private final StringRedisTemplate redisTemplate;
    private final Clock clock;

    public SubscriptionUsage findSubscriptionUsage(
            Long subId,
            DataPeriod dataPeriod
    ) {

        LocalDate now = LocalDate.now(clock);

        List<String> giftIds = new ArrayList<>(
                Objects.requireNonNull(redisTemplate.opsForZSet()
                        .range(
                                SubscriptionUsageRedisKeyBuilder.giftIndex(subId, now),
                                0,
                                -1
                        ))
        );

        PipelineResult pipeline =
                executePipeline(subId, giftIds, now, dataPeriod);

        Map<String, Object> resultMap =
                PipelineResultMapper.toMap(
                        pipeline.requestKeys(),
                        pipeline.rawResults()
                );

        return buildDomain(subId, giftIds, resultMap);
    }

    private PipelineResult executePipeline(
            Long subId,
            List<String> giftIds,
            LocalDate now,
            DataPeriod dataPeriod
    ) {

        List<String> requestKeys = new ArrayList<>();

        List<Object> rawResults =
                pipelineExecutor.execute((RedisCallback<Object>) connection -> {

                    addPlanRequests(connection, subId, dataPeriod, requestKeys, now);
                    addGiftRequests(connection, subId, giftIds, requestKeys, now);

                    return null;
                });

        return new PipelineResult(requestKeys, rawResults);
    }

    private void addPlanRequests(
            RedisConnection connection,
            Long subId,
            DataPeriod dataPeriod,
            List<String> requestKeys,
            LocalDate now
    ) {

        requestKeys.add(K_PLAN_LIMIT);
        connection.hGet(
                pipelineExecutor.serialize(
                        SubscriptionUsageRedisKeyBuilder.planLimit(subId)
                ),
                pipelineExecutor.serialize(K_PLAN_LIMIT)
        );

        requestKeys.add(K_PLAN_USED);

        String usageKey =
                (dataPeriod == DataPeriod.MONTH)
                        ? SubscriptionUsageRedisKeyBuilder.planUsageMonth(subId, now)
                        : SubscriptionUsageRedisKeyBuilder.planUsageDay(subId, now);

        connection.hGet(
                pipelineExecutor.serialize(usageKey),
                pipelineExecutor.serialize(K_PLAN_USED)
        );
    }

    private void addGiftRequests(
            RedisConnection connection,
            Long subId,
            List<String> giftIds,
            List<String> requestKeys,
            LocalDate now
    ) {

        if (giftIds == null || giftIds.isEmpty()) {
            return;
        }

        for (String giftIdStr : giftIds) {

            Long giftId = Long.parseLong(giftIdStr);

            requestKeys.add(K_GIFT_LIMIT_PREFIX + giftId);
            connection.hGet(
                    pipelineExecutor.serialize(
                            SubscriptionUsageRedisKeyBuilder.giftLimit(subId, giftId, now)
                    ),
                    pipelineExecutor.serialize("gift_limit")
            );

            requestKeys.add(K_GIFT_USED_PREFIX + giftId);
            connection.hGet(
                    pipelineExecutor.serialize(
                            SubscriptionUsageRedisKeyBuilder.giftUsage(subId, giftId, now)
                    ),
                    pipelineExecutor.serialize("gift_used")
            );
        }
    }

    private SubscriptionUsage buildDomain(
            Long subId,
            List<String> giftIds,
            Map<String, Object> resultMap
    ) {

        Object planLimitValue = resultMap.get(K_PLAN_LIMIT);

        if (planLimitValue == null) {
            throw new ApplicationException(
                    SubscriptionUsageErrorCode.SUBSCRIPTION_LIMIT_NOT_FOUND
            );
        }

        double planLimitKb =
                RedisValueParser.toDouble(planLimitValue);

        Object planUsedValue = resultMap.get(K_PLAN_USED);

        double planUsedKb = planUsedValue == null
                ? 0D
                : RedisValueParser.toDouble(planUsedValue);

        List<GiftUsage> gifts =
                buildGiftUsageList(giftIds, resultMap);

        return new SubscriptionUsage(
                subId,
                planLimitKb,
                planUsedKb,
                gifts
        );
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
