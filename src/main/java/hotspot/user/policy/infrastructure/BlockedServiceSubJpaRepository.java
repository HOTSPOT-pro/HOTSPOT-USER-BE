package hotspot.user.policy.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.policy.infrastructure.entity.BlockedServiceSubEntity;

import java.util.List;

public interface BlockedServiceSubJpaRepository extends JpaRepository<BlockedServiceSubEntity, Long> {
    List<BlockedServiceSubEntity> findBySubscriptionSubId(Long subId);
}
