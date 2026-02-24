package hotspot.user.presentData.infrastructure;

import java.time.LocalDate;
import java.util.*;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.presentData.domain.SubUsage;
import hotspot.user.presentData.infrastructure.keybuilder.FamilySubUsageRedisKeyBuilder;
import hotspot.user.presentData.infrastructure.mapper.FamilySubUsageMapper;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilySubUsageRedisRepository {

    private final StringRedisTemplate redisTemplate;
    private final FamilySubUsageMapper mapper;

    public Map<Long, SubUsage> findUsageAndLimit(
            Map<Long, DataPeriod> subPeriodMap
    ) {

        LocalDate now = LocalDate.now();
        List<Long> subIds = new ArrayList<>(subPeriodMap.keySet());

        List<Object> results = redisTemplate.executePipelined(
                (RedisConnection connection) -> {

                    for (Long subId : subIds) {

                        DataPeriod period = subPeriodMap.get(subId);

                        String usageKey = (period == DataPeriod.DAY)
                                ? FamilySubUsageRedisKeyBuilder.subUsageDay(subId, now)
                                : FamilySubUsageRedisKeyBuilder.subUsageMonth(subId, now);

                        String limitKey =
                                FamilySubUsageRedisKeyBuilder.subLimit(subId);

                        connection.hGet(
                                usageKey.getBytes(),
                                "plan_used".getBytes()
                        );

                        connection.hGet(
                                limitKey.getBytes(),
                                "plan_limit".getBytes()
                        );
                    }

                    return null;
                });

        return mapper.map(subIds, results);
    }
}
