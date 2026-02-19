package hotspot.user.policy.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.infrastructure.entity.BlockedServiceSubEntity;
import hotspot.user.policy.service.port.BlockedServiceSubRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BlockedServiceSubRepositoryImpl implements BlockedServiceSubRepository {
    private final BlockedServiceSubJpaRepository jpaRepository;

    @Override
    public List<BlockedServiceSub> findBySubId(Long subId) {
        return jpaRepository.findBySubscriptionSubId(subId).stream()
                .map(BlockedServiceSubEntity::entityToDomain)
                .toList();
    }
}
