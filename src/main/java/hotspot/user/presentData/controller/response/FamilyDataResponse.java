package hotspot.user.presentData.controller.response;

import java.util.List;

public record FamilyDataResponse(
        Long selfSubId,
        Double selfDataRemainAmount,
        List<subUsageResponse> subUsages
) {
    public record subUsageResponse(
            Long subId,
            String subName,
            Double subDataLimitAmount,
            Double subDataUsageAmount,
            Integer dataUsagePercent
    ) {
    }
}
