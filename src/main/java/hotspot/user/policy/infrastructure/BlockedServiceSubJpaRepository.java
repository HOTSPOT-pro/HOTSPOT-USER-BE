package hotspot.user.policy.infrastructure;

import hotspot.user.policy.infrastructure.entity.BlockedServiceSubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedServiceSubJpaRepository extends JpaRepository<BlockedServiceSubEntity, Long> {
}
