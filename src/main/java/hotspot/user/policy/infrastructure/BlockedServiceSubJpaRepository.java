package hotspot.user.policy.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.policy.infrastructure.entity.BlockedServiceSubEntity;

public interface BlockedServiceSubJpaRepository extends JpaRepository<BlockedServiceSubEntity, Long> {
    List<BlockedServiceSubEntity> findBySubscriptionSubId(Long subId);
}
