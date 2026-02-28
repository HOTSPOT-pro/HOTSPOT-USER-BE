package hotspot.user.policy.infrastructure;

import java.util.ArrayList;
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

    @Override
    public List<PolicySub> findActiveBySubId(Long subId) {
        return policySubJpaRepository.findBySubscriptionSubIdAndIsActiveTrue(subId).stream()
                .map(PolicySubEntity::entityToDomain)
                .toList();
    }


    // 정책-회선 매핑 리스트 저장
    // 새로운 INSERT, 기존 isActive 업데이트 분리해서 진행 (N+1 방지)
    @Override
    public List<PolicySub> saveAll(List<PolicySub> policySubList) {
        List<PolicySubEntity> entitiesToInsert = new ArrayList<>();
        List<Long> idsToDeactivate = new ArrayList<>();
        List<Long> idsToActivate = new ArrayList<>();

        // 단 한 번의 순회로 Insert 대상과 Update(Delete/Activate) 대상을 분류
        policySubList.forEach(domain -> {
            if (domain.getId() == null) {
                entitiesToInsert.add(PolicySubEntity.domainToEntity(domain));
            } else if (!domain.isActive()) {
                idsToDeactivate.add(domain.getId());
            } else {
                idsToActivate.add(domain.getId());
            }
        });

        List<PolicySubEntity> savedEntities = new ArrayList<>();

        // 1. 신규 Insert 실행
        if (!entitiesToInsert.isEmpty()) {
            savedEntities = policySubJpaRepository.saveAll(entitiesToInsert);
        }

        // 2. 벌크 비활성화 실행 (isActive = false로 변경)
        if (!idsToDeactivate.isEmpty()) {
            policySubJpaRepository.bulkDeActive(idsToDeactivate);
        }

        // 3. 벌크 활성화 실행 (isActive = true로 변경)
        if (!idsToActivate.isEmpty()) {
            policySubJpaRepository.bulkActivate(idsToActivate);
        }

        return savedEntities.stream()
                .map(PolicySubEntity::entityToDomain)
                .toList();
    }
}
