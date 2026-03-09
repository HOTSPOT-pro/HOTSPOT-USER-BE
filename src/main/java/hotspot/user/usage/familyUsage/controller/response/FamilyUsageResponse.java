package hotspot.user.usage.familyUsage.controller.response;

import java.time.LocalDateTime;
import java.util.List;

public record FamilyUsageResponse(
        LocalDateTime currentTime,
        Double familyDataAmount,
        Double familyDataUsageAmount,
        Double familyDataRemainAmount,
        Integer remainDataPercent,
        List<FamilySubUsageResponse> subUsages
) {

    public record FamilySubUsageResponse(
            Long subId,
            String subName,
            Double dataLimit,
            Double dataUsageAmount,
            Double dataRemainAmount,
            Integer remainDataPercent
    ) {
    }
}
