package hotspot.user.policy.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.infrastructure.entity.AppBlockedServiceEntity;
import hotspot.user.policy.infrastructure.entity.BlockedServiceSubEntity;
import hotspot.user.policy.service.port.BlockedServiceSubRepository;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;
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

    @Override
    public List<Long> findActiveServiceIdsBySubId(Long subId) {
        return jpaRepository.findActiveServiceIdsBySubId(subId);
    }

    @Override
    @Transactional
    public List<BlockedServiceSub> saveAll(List<BlockedServiceSub> domains) {
        List<BlockedServiceSubEntity> entitiesToInsert = new ArrayList<>();
        List<Long> idsToDeactivate = new ArrayList<>();
        List<Long> idsToActivate = new ArrayList<>();

        domains.forEach(domain -> {
            if (domain.getId() == null) {
                entitiesToInsert.add(BlockedServiceSubEntity.domainToEntity(domain));
            } else if (!domain.isActive()) {
                idsToDeactivate.add(domain.getId());
            } else {
                idsToActivate.add(domain.getId());
            }
        });

        List<BlockedServiceSubEntity> savedEntities = new ArrayList<>();

        if (!entitiesToInsert.isEmpty()) {
            savedEntities = jpaRepository.saveAll(entitiesToInsert);
        }

        if (!idsToDeactivate.isEmpty()) {
            jpaRepository.bulkDeactive(idsToDeactivate);
        }

        if (!idsToActivate.isEmpty()) {
            jpaRepository.bulkActivate(idsToActivate);
        }

        return savedEntities.stream()
                .map(BlockedServiceSubEntity::entityToDomain)
                .toList();
    }
}
