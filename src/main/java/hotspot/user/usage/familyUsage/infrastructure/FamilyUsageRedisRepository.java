package hotspot.user.usage.familyUsage.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyUsageErrorCode;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Repository;

import hotspot.user.usage.familyUsage.domain.FamilyUsage;
import hotspot.user.usage.familyUsage.domain.FamilySubUsage;
import hotspot.user.usage.familyUsage.infrastructure.util.FamilyUsageRedisKeyBuilder;
import hotspot.user.usage.familyUsage.infrastructure.util.RedisPipelineExecutor;
import hotspot.user.usage.familyUsage.infrastructure.util.RedisValueParser;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyUsageRedisRepository {

    private static final String K_FAMILY_LIMIT = "family_limit";
    private static final String K_FAMILY_USED  = "family_used";
    private static final String K_SUB_LIMIT_PREFIX = "sub_limit:";
    private static final String K_SUB_USAGE_PREFIX = "sub_usage:";

    private final RedisPipelineExecutor pipelineExecutor;

    public FamilyUsage findFamilyAndSubData(Long familyId, List<Long> subIds) {

        PipelineResult pipeline = executePipeline(familyId, subIds);
        Map<String, Object> resultMap = toResultMap(pipeline);

        Object familyLimitValue = resultMap.get(K_FAMILY_LIMIT);

        if (familyLimitValue == null) {
            throw new ApplicationException(FamilyUsageErrorCode.FAMILY_LIMIT_NOT_FOUND);
        }


        double familyLimitKb = RedisValueParser.toDouble(familyLimitValue);
        double familyUsedKb  = RedisValueParser.toDouble(resultMap.get(K_FAMILY_USED));

        Map<Long, FamilySubUsage> subMap = buildSubUsageMap(subIds, resultMap);

        return new FamilyUsage(familyLimitKb, familyUsedKb, subMap);
    }

    private PipelineResult executePipeline(Long familyId, List<Long> subIds) {

        List<String> requestKeys = new ArrayList<>();

        List<Object> rawResults = pipelineExecutor.execute((RedisCallback<Object>) connection -> {

            addFamilyRequests(connection, familyId, requestKeys);
            addSubRequests(connection, familyId, subIds, requestKeys);

            return null;
        });

        return new PipelineResult(requestKeys, rawResults);
    }

    private void addFamilyRequests(
            RedisConnection connection,
            Long familyId,
            List<String> requestKeys
    ) {
        requestKeys.add(K_FAMILY_LIMIT);
        connection.hGet(
                pipelineExecutor.serialize(FamilyUsageRedisKeyBuilder.familyLimit(familyId)),
                pipelineExecutor.serialize("family_limit")
        );

        requestKeys.add(K_FAMILY_USED);
        connection.hGet(
                pipelineExecutor.serialize(FamilyUsageRedisKeyBuilder.familyUsage(familyId)),
                pipelineExecutor.serialize("family_used")
        );
    }

    private void addSubRequests(
            RedisConnection connection,
            Long familyId,
            List<Long> subIds,
            List<String> requestKeys
    ) {
        for (Long subId : subIds) {

            requestKeys.add(K_SUB_LIMIT_PREFIX + subId);
            connection.hGet(
                    pipelineExecutor.serialize(
                            FamilyUsageRedisKeyBuilder.familySubLimit(familyId, subId)
                    ),
                    pipelineExecutor.serialize("family_limit")
            );

            requestKeys.add(K_SUB_USAGE_PREFIX + subId);
            connection.hGet(
                    pipelineExecutor.serialize(FamilyUsageRedisKeyBuilder.subUsage(subId)),
                    pipelineExecutor.serialize("member_family_used")
            );
        }
    }


    private Map<String, Object> toResultMap(PipelineResult pipeline) {

        List<Object> raw = pipeline.rawResults() == null ? List.of() : pipeline.rawResults();
        List<String> keys = pipeline.requestKeys();

        Map<String, Object> resultMap = new HashMap<>(keys.size());

        int size = Math.min(keys.size(), raw.size());
        for (int i = 0; i < size; i++) {
            resultMap.put(keys.get(i), raw.get(i));
        }

        for (int i = size; i < keys.size(); i++) {
            resultMap.put(keys.get(i), null);
        }

        return resultMap;
    }


    private Map<Long, FamilySubUsage> buildSubUsageMap(
            List<Long> subIds,
            Map<String, Object> resultMap
    ) {

        Map<Long, FamilySubUsage> subMap = new HashMap<>(subIds.size());

        for (Long subId : subIds) {
            Object subLimitValue =
                    resultMap.get(K_SUB_LIMIT_PREFIX + subId);

            if (subLimitValue == null) {
                throw new ApplicationException(FamilyUsageErrorCode.FAMILY_SUB_LIMIT_NOT_FOUND);
            }

            double subLimitKb =
                    RedisValueParser.toDouble(subLimitValue);

            double memberFamilyUsedKb =
                    RedisValueParser.toDouble(
                            resultMap.get(
                                    K_SUB_USAGE_PREFIX + subId
                            )
                    );

            subMap.put(subId, new FamilySubUsage(subLimitKb, memberFamilyUsedKb));
        }

        return subMap;
    }

    private record PipelineResult(
            List<String> requestKeys,
            List<Object> rawResults
    ) {}
}
