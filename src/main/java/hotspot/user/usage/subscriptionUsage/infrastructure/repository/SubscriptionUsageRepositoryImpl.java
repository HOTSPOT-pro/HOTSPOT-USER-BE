package hotspot.user.usage.subscriptionUsage.infrastructure.repository;

import org.springframework.stereotype.Repository;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;
import hotspot.user.usage.subscriptionUsage.service.port.SubscriptionUsageRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubscriptionUsageRepositoryImpl
        implements SubscriptionUsageRepository {

    private final SubscriptionUsageRedisRepository redisRepository;

    @Override
    public SubscriptionUsage findSubscriptionUsage(
            Long subId,
            DataPeriod dataPeriod) {
        return redisRepository.findSubscriptionUsage(subId, dataPeriod);
    }

    @Override
    public long findRemainingPlanKb(
            Long subId,
            DataPeriod dataPeriod
    ) {
        return redisRepository.findRemainingPlanKb(subId, dataPeriod);
    }
}
