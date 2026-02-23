package hotspot.user.usage.subscriptionUsage.service.port;

import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;

public interface SubscriptionUsageRepository {

    SubscriptionUsage findSubscriptionUsage(Long subId);
}
