package hotspot.user.subscription.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepository {
    private final SubscriptionJpaRepository subscriptionJpaRepository;

    @Override
    public Optional<Subscription> findById(Long id) {
        return subscriptionJpaRepository.findById(id)
                .map(SubscriptionEntity::entityToDomain);
    }

    @Override
    public Optional<Subscription> findByMemberId(Long memberId) {
        return subscriptionJpaRepository.findByMemberId(memberId)
                .map(SubscriptionEntity::entityToDomain);
    }
}
