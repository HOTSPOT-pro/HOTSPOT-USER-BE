package hotspot.user.usage.subscriptionUsage.controller.response;

import java.time.LocalDateTime;
import java.util.List;

public record SubscriptionUsageResponse(
        Long subId,
        LocalDateTime currentTime,
        String planName, // 요금제 이름
        Double subDataAmount,
        Double subDataUsageAmount,
        Double subDataRemainAmount,
        Integer dataRemainPercent,
        Double giftDataAmount,
        Double giftDataUsageAmount,
        Double giftDataRemainAmount,
        Integer giftRemainPercent,
        List<GiftUsageResponse> giftUsages
) {

    public record GiftUsageResponse(
            Long giftId,
            String giftUserName,
            Double giftDataLimit,
            Double giftDataUsageAmount,
            Double giftDataUsageRemainAmount,
            Integer dataRemainPercent
    ) {
    }
}
