package hotspot.user.policy.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.policy.infrastructure.entity.BlockPolicyEntity;

public interface BlockPolicyJpaRepository extends JpaRepository<BlockPolicyEntity, Long> {
}
