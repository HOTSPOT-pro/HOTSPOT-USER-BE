package hotspot.user.usage.subscriptionUsage.controller.response;

import java.time.LocalDateTime;

public record SubscriptionUsageResponse(
        Long subId,
        LocalDateTime currentTime,
        String planName, // 요금제 이름
        Double subDataAmount,
        Double subDataUsageAmount,
        Double subDataRemainAmount,
        Integer dataRemainPercent
) {
}
