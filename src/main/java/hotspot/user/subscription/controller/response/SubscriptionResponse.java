package hotspot.user.subscription.controller.response;

import hotspot.user.plan.controller.response.PlanResponse;
import lombok.Builder;

@Builder
public record SubscriptionResponse(
        Long id,
        String phone,
        PlanResponse plan,
        boolean isLocked
) {
}
