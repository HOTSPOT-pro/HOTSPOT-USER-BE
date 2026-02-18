package hotspot.user.family.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.infrastructure.entity.FamilySubscriptionEntity;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilySubscriptionRepositoryImpl implements FamilySubscriptionRepository {

    private final FamilySubscriptionJpaRepository jpaRepository;

    @Override
    public Optional<FamilySubscription> findBySubId(Long subId) {
        return jpaRepository.findBySubId(subId)
                .map(FamilySubscriptionEntity::entityToDomain);
    }
}
