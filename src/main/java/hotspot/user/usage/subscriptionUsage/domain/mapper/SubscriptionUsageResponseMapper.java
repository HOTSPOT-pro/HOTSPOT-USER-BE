package hotspot.user.usage.subscriptionUsage.domain.mapper;

import java.time.LocalDateTime;

import hotspot.user.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;
import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;

public final class SubscriptionUsageResponseMapper {

    private SubscriptionUsageResponseMapper() {}

    public static SubscriptionUsageResponse toResponse(
            SubscriptionUsage usage,
            String planName,
            LocalDateTime now
    ) {

        return new SubscriptionUsageResponse(
                usage.subId(),
                now,
                planName,
                usage.limitGb(),
                usage.usedGb(),
                usage.remainGb(),
                usage.remainPercent()
        );
    }
}
