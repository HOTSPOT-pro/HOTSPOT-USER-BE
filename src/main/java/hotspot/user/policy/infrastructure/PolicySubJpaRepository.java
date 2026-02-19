package hotspot.user.policy.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.policy.infrastructure.entity.PolicySubEntity;

import java.util.List;

public interface PolicySubJpaRepository extends JpaRepository<PolicySubEntity, Long> {
    List<PolicySubEntity> findBySubscriptionSubId(Long subId);
}
