package hotspot.user.usage.subscriptionUsage.service.port;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;

public interface SubscriptionUsageRepository {

    SubscriptionUsage findSubscriptionUsage(
            Long subId,
            DataPeriod dataPeriod);

    long findRemainingPlanKb(Long subId, DataPeriod dataPeriod);
}
