package hotspot.user.family.controller.response;

import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.PriorityType;

/**
 * 가족 응답 Dto
 */
public record FamilyResponse(
        Long id,
        int familyNum,
        int familyDataAmount,
        PriorityType priorityType
) {
    public static FamilyResponse from(Family family) {
        return new FamilyResponse(
                family.getId(),
                family.getFamilyNum(),
                family.getFamilyDataAmount(),
                family.getPriorityType()
        );
    }
}
