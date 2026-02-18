package hotspot.user.plan.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.plan.infrastructure.entity.PlanEntity;

/**
 * DB에 저장 하는 실제 Repossitory
 */
public interface PlanJpaRepository extends JpaRepository<PlanEntity, Long> {
    Optional<PlanEntity> findByPlanId(Long planId);
}
