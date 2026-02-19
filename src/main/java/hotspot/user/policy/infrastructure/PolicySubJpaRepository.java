package hotspot.user.policy.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.policy.infrastructure.entity.PolicySubEntity;

public interface PolicySubJpaRepository extends JpaRepository<PolicySubEntity, Long> {
}
