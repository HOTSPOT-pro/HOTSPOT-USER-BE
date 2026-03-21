package hotspot.user.usage.subscriptionUsage.controller.port;

import hotspot.user.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;

public interface FindSubscriptionUsageService {

    SubscriptionUsageResponse findSubscriptionUsage(Long subscriptionId);
}
