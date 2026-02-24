package hotspot.user.subscription.domain.mapper;

import hotspot.user.plan.domain.mapper.PlanMapper;
import hotspot.user.subscription.controller.response.SubscriptionResponse;
import hotspot.user.subscription.domain.Subscription;

/**
 * request Dto -> 도메인
 * 도메인 -> response Dto
 */
public class SubscriptionMapper {
    // request -> domain

    // domain -> response
    public static SubscriptionResponse toSubscriptionResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .phone(subscription.getPhone())
                .plan(PlanMapper.toPlanResponse(subscription.getPlan()))
                .isLocked(subscription.getIsLocked())
                .build();
    }
}
