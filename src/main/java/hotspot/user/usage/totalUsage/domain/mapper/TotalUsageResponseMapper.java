package hotspot.user.usage.totalUsage.domain.mapper;

import java.time.LocalDateTime;

import hotspot.user.subscription.domain.Subscription;
import hotspot.user.usage.totalUsage.controller.response.TotalUsageResponse;
import hotspot.user.usage.totalUsage.repository.schema.TotalUsage;

public final class TotalUsageResponseMapper {

    private TotalUsageResponseMapper() {}

    public static TotalUsageResponse toTotalUsageResponse(
            Subscription subscription,
            TotalUsage usage,
            LocalDateTime now
    ) {

        return new TotalUsageResponse(
                subscription.getId(),
                now,
                usage.totalDataAmount(),
                usage.totalDataRemainAmount(),
                usage.totalDataRemainPercent(),
                subscription.getPlan().getName(),
                usage.subDataRemainAmount(),
                usage.giftDataRemainAmount(),
                usage.familyDataRemainAmount()
        );
    }
}
