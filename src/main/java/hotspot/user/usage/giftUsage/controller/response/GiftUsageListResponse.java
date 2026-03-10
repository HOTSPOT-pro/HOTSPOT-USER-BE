package hotspot.user.usage.giftUsage.controller.response;

import java.time.LocalDateTime;
import java.util.List;

public record GiftUsageListResponse(
        LocalDateTime currentTime,
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
    ) {}
}
