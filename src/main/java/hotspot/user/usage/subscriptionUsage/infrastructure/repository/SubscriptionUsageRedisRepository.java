package hotspot.user.usage.subscriptionUsage.infrastructure.repository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Repository;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SubscriptionUsageErrorCode;
import hotspot.user.common.util.redis.PipelineResultMapper;
import hotspot.user.common.util.redis.RedisPipelineExecutor;
import hotspot.user.common.util.redis.RedisValueParser;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;
import hotspot.user.usage.subscriptionUsage.infrastructure.keybuilder.SubscriptionUsageRedisKeyBuilder;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubscriptionUsageRedisRepository {

    private static final String K_PLAN_LIMIT = "plan_limit";
    private static final String K_PLAN_USED = "plan_used";

    private final RedisPipelineExecutor pipelineExecutor;
    private final Clock clock;

    public SubscriptionUsage findSubscriptionUsage(
            Long subId,
            DataPeriod dataPeriod
    ) {

        LocalDate now = LocalDate.now(clock);

        PipelineResult pipeline =
                executePlanOnlyPipeline(subId, dataPeriod, now);

        Map<String, Object> resultMap =
                PipelineResultMapper.toMap(
                        pipeline.requestKeys(),
                        pipeline.rawResults()
                );

        return buildDomain(subId, resultMap);
    }

    private PipelineResult executePlanOnlyPipeline(
            Long subId,
            DataPeriod dataPeriod,
            LocalDate now
    ) {

        List<String> requestKeys = new ArrayList<>();

        List<Object> rawResults =
                pipelineExecutor.execute((RedisCallback<Object>) connection -> {

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

                    return null;
                });

        return new PipelineResult(requestKeys, rawResults);
    }

    private SubscriptionUsage buildDomain(
            Long subId,
            Map<String, Object> resultMap
    ) {

        double planLimitKb =
                RedisValueParser.toDouble(resultMap.get(K_PLAN_LIMIT));

        double planUsedKb =
                resultMap.get(K_PLAN_USED) == null
                        ? 0D
                        : RedisValueParser.toDouble(resultMap.get(K_PLAN_USED));

        System.out.println("plan_limit raw = " + resultMap.get(K_PLAN_LIMIT));
        System.out.println("plan_used raw = " + resultMap.get(K_PLAN_USED));

        return new SubscriptionUsage(
                subId,
                planLimitKb,
                planUsedKb
        );
    }

    private record PipelineResult(
            List<String> requestKeys,
            List<Object> rawResults
    ) {}

    public long findRemainingPlanKb(
            Long subId,
            DataPeriod dataPeriod
    ) {

        LocalDate now = LocalDate.now(clock);

        PipelineResult pipeline =
                executePlanOnlyPipeline(subId, dataPeriod, now);

        Map<String, Object> resultMap =
                PipelineResultMapper.toMap(
                        pipeline.requestKeys(),
                        pipeline.rawResults()
                );

        return buildRemaining(resultMap);
    }

    private long buildRemaining(Map<String, Object> resultMap) {

        Object planLimitValue = resultMap.get(K_PLAN_LIMIT);

        if (planLimitValue == null) {
            throw new ApplicationException(
                    SubscriptionUsageErrorCode.SUBSCRIPTION_LIMIT_NOT_FOUND
            );
        }

        long planLimitKb = RedisValueParser.toLong(planLimitValue);

        Object planUsedValue = resultMap.get(K_PLAN_USED);

        long planUsedKb = (planUsedValue == null)
                ? 0L
                : RedisValueParser.toLong(planUsedValue);

        return Math.max(planLimitKb - planUsedKb, 0L);
    }
}
