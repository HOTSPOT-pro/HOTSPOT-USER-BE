package hotspot.user.policy.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.policy.infrastructure.entity.PolicySubEntity;

public interface PolicySubJpaRepository extends JpaRepository<PolicySubEntity, Long> {
    List<PolicySubEntity> findBySubscriptionSubId(Long subId);
}
