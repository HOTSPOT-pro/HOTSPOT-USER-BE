package hotspot.user.usage.totalUsage.controller.response;

import java.time.LocalDateTime;

public record TotalUsageResponse(
        Long subId,
        LocalDateTime currentTime,
        Double totalDataAmount,
        Double totalDataRemainAmount,
        Integer totalDataRemainPercent,
        String planName,
        Double subDataRemainAmount,
        Double giftDataRemainAmount,
        Double familyDataRemainAmount
) {
}
