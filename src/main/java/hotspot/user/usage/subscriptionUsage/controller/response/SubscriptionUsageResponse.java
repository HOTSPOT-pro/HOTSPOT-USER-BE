package hotspot.user.usage.subscriptionUsage.controller.response;

import java.time.LocalDateTime;
import java.util.List;

public record SubscriptionUsageResponse(
        Long subId,
        LocalDateTime currentTime,
        Double subDataAmount,
        Double subDataUsageAmount,
        Double subDataRemainAmount,
        Integer dataUsagePercent,
        Double giftDataAmount,
        Double giftDataUsageAmount,
        Double giftDataRemainAmount,
        Integer giftUsagePercent,
        List<GiftUsageResponse> giftUsages
) {

    public record GiftUsageResponse(
            Long giftId,
            String giftUserName,
            Double giftDataLimit,
            Double giftDataUsageAmount,
            Double giftDataUsageRemainAmount,
            Integer dataUsagePercent
    ) {
    }
}
