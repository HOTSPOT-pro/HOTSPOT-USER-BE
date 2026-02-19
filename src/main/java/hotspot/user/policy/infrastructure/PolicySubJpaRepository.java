package hotspot.user.policy.infrastructure;

import hotspot.user.policy.infrastructure.entity.PolicySubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicySubJpaRepository extends JpaRepository<PolicySubEntity, Long> {
}
