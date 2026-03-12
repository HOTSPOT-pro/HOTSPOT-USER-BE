package hotspot.user.family.controller.response;

import hotspot.user.common.util.UsageCalculator;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.PriorityType;

/**
 * 가족 응답 Dto
 */
public record FamilyResponse(
        Long id,
        int familyNum,
        double familyDataAmount,
        PriorityType priorityType
) {
    public static FamilyResponse from(Family family) {
        return new FamilyResponse(
                family.getId(),
                family.getFamilyNum(),
                UsageCalculator.kbToGb(family.getFamilyDataAmount()),
                family.getPriorityType()
        );
    }
}
