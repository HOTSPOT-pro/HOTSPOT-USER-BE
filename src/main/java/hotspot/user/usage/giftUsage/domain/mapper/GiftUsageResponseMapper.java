package hotspot.user.usage.giftUsage.domain.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import hotspot.user.usage.giftUsage.controller.response.GiftUsageListResponse;
import hotspot.user.usage.giftUsage.domain.GiftUsage;

public final class GiftUsageResponseMapper {

    private GiftUsageResponseMapper() {}

    public static GiftUsageListResponse toGiftUsageListResponse(
            LocalDateTime currentTime,
            List<GiftUsage> gifts,
            Map<Long, String> giftIdToUserName
    ) {

        double totalLimit = gifts.stream()
                .mapToDouble(GiftUsage::limitGb)
                .sum();

        double totalUsed = gifts.stream()
                .mapToDouble(GiftUsage::usedGb)
                .sum();

        double totalRemain = gifts.stream()
                .mapToDouble(GiftUsage::remainGb)
                .sum();

        int remainPercent =
                totalLimit == 0
                        ? 0
                        : (int) ((totalRemain / totalLimit) * 100);

        List<GiftUsageListResponse.GiftUsageResponse> responses =
                gifts.stream()
                        .map(gift -> new GiftUsageListResponse.GiftUsageResponse(
                                gift.giftId(),
                                giftIdToUserName.getOrDefault(gift.giftId(), "Unknown"),
                                gift.limitGb(),
                                gift.usedGb(),
                                gift.remainGb(),
                                gift.remainPercent()
                        ))
                        .toList();

        return new GiftUsageListResponse(
                currentTime,
                totalLimit,
                totalUsed,
                totalRemain,
                remainPercent,
                responses
        );
    }
}
