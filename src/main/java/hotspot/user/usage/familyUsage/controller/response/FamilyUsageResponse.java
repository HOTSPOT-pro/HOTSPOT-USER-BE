package hotspot.user.usage.familyUsage.controller.response;

import java.util.List;

public record FamilyUsageResponse(
        Double familyDataAmount,
        Double familyDataUsageAmount,
        Double familyDataRemainAmount,
        Integer dataUsagePercent,
        List<FamilySubUsageResponse> subUsages
) {

    public record FamilySubUsageResponse(
            Long subId,
            String subName,
            Double dataLimit,
            Double dataUsageAmount,
            Double dataUsageRemainAmount,
            Integer dataUsagePercent
    ) {
    }
}
