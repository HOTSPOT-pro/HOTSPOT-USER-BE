package hotspot.user.plan.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.plan.domain.Plan;
import hotspot.user.plan.infrastructure.entity.PlanEntity;
import hotspot.user.plan.service.port.PlanRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PlanRepositoryImpl implements PlanRepository {
    private final PlanJpaRepository planJpaRepository;

    @Override
    public Optional<Plan> findById(Long id) {
        return planJpaRepository.findByPlanId(id)
                .map(PlanEntity::entityToDomain);
    }
}
