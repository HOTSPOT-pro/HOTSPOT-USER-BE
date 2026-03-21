package hotspot.user.policy.infrastructure.redis;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import hotspot.user.common.util.UsageCalculator;
import hotspot.user.common.util.redis.RedisPipelineExecutor;
import hotspot.user.common.util.redis.RedisValueParser;
import hotspot.user.policy.infrastructure.schema.FamilyDataControl;
import hotspot.user.policy.service.port.FamilyDataLimitRepository;
import hotspot.user.usage.familyUsage.infrastructure.keybuilder.FamilyUsageRedisKeyBuilder;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyDataControlRedisRepository implements FamilyDataLimitRepository {

    private final StringRedisTemplate redisTemplate;
    private final RedisPipelineExecutor pipelineExecutor;

    @Override
    public FamilyDataControl findFamilyDataLimit(Long familyId) {

        YearMonth now = YearMonth.now();
        LocalDate date = now.atDay(1);

        Long familyLimitGb = getFamilyLimit(familyId);

        Set<String> subIds = getFamilySubIds(familyId);

        if (subIds == null || subIds.isEmpty()) {
            return new FamilyDataControl(familyLimitGb, List.of());
        }

        List<String> subIdList = new ArrayList<>(subIds);
        subIdList.sort(Comparator.comparingLong(Long::parseLong));

        List<Object> rawResults =
                fetchSubUsageWithPipeline(subIdList, date);

        if (rawResults == null) {
            return new FamilyDataControl(familyLimitGb, List.of());
        }


        List<FamilyDataControl.SubFamilyDataControl> subFamilies =
                buildSubFamilyControls(subIdList, rawResults);

        return new FamilyDataControl(familyLimitGb, subFamilies);
    }

    /**
     * 가족 전체 데이터 한도 조회
     */
    private Long getFamilyLimit(Long familyId) {

        String key = FamilyUsageRedisKeyBuilder.familyLimit(familyId);

        Long familyLimitKb = RedisValueParser.toLong(
                redisTemplate.opsForHash().get(key, "family_limit")
        );

        return UsageCalculator.kbToGbCeil(
                familyLimitKb == null ? 0 : familyLimitKb
        );
    }

    /**
     * 가족 구성원 조회
     */
    private Set<String> getFamilySubIds(Long familyId) {

        String key = FamilyUsageRedisKeyBuilder.familySubs(familyId);

        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Redis pipeline 조회
     */
    private List<Object> fetchSubUsageWithPipeline(
            List<String> subIds,
            LocalDate date
    ) {

        return pipelineExecutor.execute((RedisConnection conn) -> {

            for (String subId : subIds) {

                Long sub = Long.parseLong(subId);

                String usageKey =
                        FamilyUsageRedisKeyBuilder.subUsage(sub, date);

                conn.hashCommands().hGet(
                        pipelineExecutor.serialize(usageKey),
                        pipelineExecutor.serialize("member_family_used")
                );
            }

            return null;
        });
    }

    /**
     * Redis 결과 → DTO 변환
     */
    private List<FamilyDataControl.SubFamilyDataControl> buildSubFamilyControls(
            List<String> subIds,
            List<Object> rawResults
    ) {

        List<FamilyDataControl.SubFamilyDataControl> result = new ArrayList<>();

        int index = 0;

        for (String subId : subIds) {

            Long subUsageKb =
                    RedisValueParser.toLong(rawResults.get(index++));

            Long subUsageGb =
                    UsageCalculator.kbToGbCeil(
                            subUsageKb == null ? 0 : subUsageKb
                    );

            result.add(
                    new FamilyDataControl.SubFamilyDataControl(
                            Long.parseLong(subId),
                            subUsageGb
                    )
            );
        }

        return result;
    }
}
