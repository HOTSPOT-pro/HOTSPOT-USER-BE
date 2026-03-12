package hotspot.user.usage.totalUsage.repository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.TotalUsageErrorCode;
import hotspot.user.common.util.redis.PipelineResultMapper;
import hotspot.user.common.util.redis.RedisPipelineExecutor;
import hotspot.user.common.util.UsageCalculator;
import hotspot.user.common.util.redis.RedisValueParser;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.usage.totalUsage.repository.keybuilder.TotalUsageRedisKeyBuilder;
import hotspot.user.usage.totalUsage.repository.schema.TotalUsage;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TotalUsageRedisRepository {

    private static final String K_PLAN_LIMIT = "plan_limit";
    private static final String K_PLAN_USED = "plan_used";
    private static final String K_FAMILY_USED = "member_family_used";
    private static final String K_FAMILY_LIMIT = "family_limit";

    private final RedisPipelineExecutor pipelineExecutor;
    private final StringRedisTemplate redisTemplate;
    private final Clock clock;

    public TotalUsage findTotalUsage(
            Long subId,
            Long familyId,
            DataPeriod dataPeriod
    ) {

        LocalDate now = LocalDate.now(clock);

        Set<String> giftIds =
                redisTemplate.opsForZSet()
                        .range(
                                TotalUsageRedisKeyBuilder.giftIndex(subId, now),
                                0,
                                -1
                        );

        List<String> requestKeys = new ArrayList<>();

        List<Object> rawResults =
                pipelineExecutor.execute((RedisCallback<Object>) connection -> {

                    String usageKey =
                            dataPeriod == DataPeriod.MONTH
                                    ? TotalUsageRedisKeyBuilder.planUsageMonth(subId, now)
                                    : TotalUsageRedisKeyBuilder.planUsageDay(subId, now);

                    requestKeys.add(K_PLAN_LIMIT);
                    connection.hGet(
                            pipelineExecutor.serialize(
                                    TotalUsageRedisKeyBuilder.planLimit(subId)),
                            pipelineExecutor.serialize(K_PLAN_LIMIT)
                    );

                    requestKeys.add(K_PLAN_USED);
                    connection.hGet(
                            pipelineExecutor.serialize(usageKey),
                            pipelineExecutor.serialize(K_PLAN_USED)
                    );

                    requestKeys.add(K_FAMILY_USED);
                    connection.hGet(
                            pipelineExecutor.serialize(usageKey),
                            pipelineExecutor.serialize(K_FAMILY_USED)
                    );

                    requestKeys.add(K_FAMILY_LIMIT);
                    connection.hGet(
                            pipelineExecutor.serialize(
                                    TotalUsageRedisKeyBuilder.familyLimit(familyId, subId)),
                            pipelineExecutor.serialize(K_FAMILY_LIMIT)
                    );

                    if (giftIds != null) {

                        for (String giftIdStr : giftIds) {

                            Long giftId = Long.parseLong(giftIdStr);

                            requestKeys.add("gift_limit:" + giftId);

                            connection.hGet(
                                    pipelineExecutor.serialize(
                                            TotalUsageRedisKeyBuilder.giftLimit(subId, giftId, now)),
                                    pipelineExecutor.serialize("gift_limit")
                            );

                            requestKeys.add("gift_used:" + giftId);

                            connection.hGet(
                                    pipelineExecutor.serialize(
                                            TotalUsageRedisKeyBuilder.giftUsage(subId, giftId, now)),
                                    pipelineExecutor.serialize("gift_used")
                            );
                        }
                    }

                    return null;
                });

        Map<String, Object> resultMap =
                PipelineResultMapper.toMap(requestKeys, rawResults);

        return buildResult(resultMap, giftIds);
    }

    private TotalUsage buildResult(
            Map<String, Object> resultMap,
            Set<String> giftIds
    ) {

        long planLimitKb =
                Optional.ofNullable(RedisValueParser.toLong(resultMap.get(K_PLAN_LIMIT)))
                        .orElseThrow(() -> new ApplicationException(TotalUsageErrorCode.PLAN_DATA_LIMIT_NOT_FOUND));

        long planUsedKb =
                Optional.ofNullable(RedisValueParser.toLong(resultMap.get(K_PLAN_USED)))
                        .orElse(0L);

        long familyUsedKb =
                Optional.ofNullable(RedisValueParser.toLong(resultMap.get(K_FAMILY_USED)))
                        .orElse(0L);

        long familyLimitKb =
                Optional.ofNullable(RedisValueParser.toLong(resultMap.get(K_FAMILY_LIMIT)))
                        .orElseThrow(() -> new ApplicationException(TotalUsageErrorCode.FAMILY_DATA_LIMIT_NOT_FOUND));

        long giftLimitTotalKb = 0;
        long giftUsedTotalKb = 0;

        if (giftIds != null) {

            for (String giftIdStr : giftIds) {

                Long giftId = Long.parseLong(giftIdStr);

                Long limit =
                        RedisValueParser.toLong(resultMap.get("gift_limit:" + giftId));

                Long used =
                        RedisValueParser.toLong(resultMap.get("gift_used:" + giftId));

                if (limit != null) {
                    giftLimitTotalKb += limit;
                }

                if (used != null) {
                    giftUsedTotalKb += used;
                }
            }
        }

        double planRemainKb =
                UsageCalculator.calculateRemain(planLimitKb, planUsedKb);

        double familyRemainKb =
                UsageCalculator.calculateRemain(familyLimitKb, familyUsedKb);

        double giftRemainKb =
                UsageCalculator.calculateRemain(giftLimitTotalKb, giftUsedTotalKb);

        double totalLimitKb =
                planLimitKb + familyLimitKb + giftLimitTotalKb;

        double totalRemainKb =
                planRemainKb + familyRemainKb + giftRemainKb;

        if (planLimitKb == -1) {
            return unlimitedResult(giftRemainKb, familyRemainKb);
        }

        return new TotalUsage(

                UsageCalculator.kbToGb(totalLimitKb),
                UsageCalculator.kbToGb(totalRemainKb),
                UsageCalculator.calculatePercent(totalRemainKb, totalLimitKb),

                UsageCalculator.kbToGb(planRemainKb),
                UsageCalculator.kbToGb(giftRemainKb),
                UsageCalculator.kbToGb(familyRemainKb)
        );
    }

    private TotalUsage unlimitedResult(double giftRemainKb, double familyRemainKb) {

        return new TotalUsage(
                -1, // total amount
                -1, // remain
                -1, // percent
                -1,
                UsageCalculator.kbToGb(giftRemainKb),
                UsageCalculator.kbToGb(familyRemainKb)
        );
    }
}
