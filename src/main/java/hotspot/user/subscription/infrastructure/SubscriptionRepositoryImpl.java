package hotspot.user.subscription.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import hotspot.user.plan.domain.DataPeriod;
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

    @Override
    public Optional<Subscription> findByPhoneHash(String phoneHash) {
        return subscriptionJpaRepository.findByPhoneHash(phoneHash)
                .map(SubscriptionEntity::entityToDomain);
    }

    @Override
    public Subscription save(Subscription subscription) {
        return subscriptionJpaRepository.save(SubscriptionEntity.domainToEntity(subscription))
                .entityToDomain();
    }

    @Override
    public Map<Long, DataPeriod> findDataPeriodsBySubIds(List<Long> subIds) {

        if (subIds == null || subIds.isEmpty()) {
            return Map.of();
        }

        return subscriptionJpaRepository
                .findAllByIdIn(subIds)
                .stream()
                .map(SubscriptionEntity::entityToDomain)
                .collect(Collectors.toMap(
                        Subscription::getId,
                        s -> s.getPlan().getDataPeriod()
                ));
    }
}
