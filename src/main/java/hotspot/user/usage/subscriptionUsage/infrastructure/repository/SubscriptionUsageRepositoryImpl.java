package hotspot.user.usage.subscriptionUsage.infrastructure.repository;

import org.springframework.stereotype.Repository;

import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;
import hotspot.user.usage.subscriptionUsage.service.port.SubscriptionUsageRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubscriptionUsageRepositoryImpl
        implements SubscriptionUsageRepository {

    private final SubscriptionUsageRedisRepository redisRepository;

    @Override
    public SubscriptionUsage findSubscriptionUsage(Long subId) {
        return redisRepository.findSubscriptionUsage(subId);
    }
}
