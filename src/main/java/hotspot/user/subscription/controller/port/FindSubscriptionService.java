package hotspot.user.subscription.controller.port;

import hotspot.user.subscription.controller.response.SubscriptionResponse;

/**
 * 회선 조회 서비스
 */
public interface FindSubscriptionService {
    SubscriptionResponse findById(Long id);
    SubscriptionResponse findByMemberId(Long memberId);
}
