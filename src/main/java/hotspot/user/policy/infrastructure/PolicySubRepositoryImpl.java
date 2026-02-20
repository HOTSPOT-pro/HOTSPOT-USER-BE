package hotspot.user.policy.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.infrastructure.entity.PolicySubEntity;
import hotspot.user.policy.service.port.PolicySubRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PolicySubRepositoryImpl implements PolicySubRepository {
    private final PolicySubJpaRepository policySubJpaRepository;

    @Override
    public List<PolicySub> findBySubId(Long subId) {
        return policySubJpaRepository.findBySubscriptionSubId(subId).stream()
                .map(PolicySubEntity::entityToDomain)
                .toList();
    }
}
