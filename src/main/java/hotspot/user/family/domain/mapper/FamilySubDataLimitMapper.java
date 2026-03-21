package hotspot.user.family.domain.mapper;

import hotspot.user.common.util.UsageCalculator;
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
                .dataLimit(UsageCalculator.kbToGb(familySubDataLimit.getDataLimit()))
                .familyDataAmount(UsageCalculator.kbToGb(familySubDataLimit.getFamilyDataAmount()))
                .build();

    }
}
