package hotspot.user.presentData.controller.response;

import java.util.List;

public record FamilyDataResponse(
        Long selfSubId,
        Double selfDataRemainAmount,
        List<SubUsageResponse> subUsages
) {
    public record SubUsageResponse(
            Long subId,
            String subName,
            Double subDataLimitAmount,
            Double subDataUsageAmount,
            Integer dataUsagePercent
    ) {
    }
}
