package hotspot.user.plan.service.port;

import java.util.Optional;

import hotspot.user.plan.domain.Plan;

/**
 * 도메인에 저장하는 Repository
 */
public interface PlanRepository {
    Optional<Plan> findById(Long id);
}
