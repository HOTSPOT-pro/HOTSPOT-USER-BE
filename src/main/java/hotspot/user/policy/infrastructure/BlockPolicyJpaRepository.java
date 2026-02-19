package hotspot.user.policy.infrastructure;

import hotspot.user.policy.infrastructure.entity.BlockPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockPolicyJpaRepository extends JpaRepository<BlockPolicyEntity, Long> {
}
