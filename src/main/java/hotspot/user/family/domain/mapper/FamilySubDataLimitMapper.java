package hotspot.user.family.domain.mapper;

import hotspot.user.common.util.redis.RedisUsageCalculator;
import hotspot.user.family.controller.response.FindDataLimitResponse;
import hotspot.user.family.domain.FamilySubDataLimit;

/**
 * 구성원 데이터 한도 도메인 <-> dto
 */
public class FamilySubDataLimitMapper {

    public static FindDataLimitResponse toFindDataLimitResponse(FamilySubDataLimit familySubDataLimit) {
        return FindDataLimitResponse.builder()
                .name(familySubDataLimit.getName())
                .isLocked(familySubDataLimit.getIsLocked())
                .dataLimit(RedisUsageCalculator.kbToGb(familySubDataLimit.getDataLimit()))
                .familyDataAmount(RedisUsageCalculator.kbToGb(familySubDataLimit.getFamilyDataAmount()))
                .build();

    }
}
