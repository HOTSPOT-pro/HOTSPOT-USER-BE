package hotspot.user.subscription.controller.response;

import hotspot.user.plan.controller.response.PlanResponse;
import hotspot.user.plan.domain.mapper.PlanMapper;
import hotspot.user.subscription.domain.Subscription;

public record SubscriptionResponse(
        Long id,
        String phone,
        PlanResponse plan,
        boolean isLocked
) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getPhoneEnc(),
                PlanMapper.toPlanResponse(subscription.getPlan()),
                subscription.getIsLocked()
        );
    }
}
