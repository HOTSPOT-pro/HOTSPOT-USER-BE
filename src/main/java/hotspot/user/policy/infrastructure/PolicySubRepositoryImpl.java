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


    // 정책-회선 매핑 리스트 저장
    // 새로운 INSERT, 기존 isDeleted=true 분리해서 진행 (N+1 방지)
    @Override
    public List<PolicySub> saveAll(List<PolicySub> policySubList) {
        // 1. 신규 Insert 대상 (ID가 없는 도메인)
        List<PolicySubEntity> entitiesToInsert = policySubList.stream()
                .filter(domain -> domain.getId() == null)
                .map(PolicySubEntity::domainToEntity)
                .toList();

        // 2. 비활성화 Update 대상 (ID가 있는 도메인의 ID값만 추출)
        List<Long> idsToUpdate = policySubList.stream()
                .filter(domain -> domain.getId() != null && domain.getIsDeleted())
                .map(PolicySub::getId)
                .toList();

        List<PolicySubEntity> savedEntities = new ArrayList<>();

        // 3. Insert 실행 (em.persist만 타므로 N+1 없음) => 무조건 새로운 row 보장 (id = null)
        if (!entitiesToInsert.isEmpty()) {
            savedEntities = policySubJpaRepository.saveAll(entitiesToInsert);
        }

        // 4. Update 실행 (벌크 연산으로 쿼리 1방에 처리, id만 전달)
        if (!idsToUpdate.isEmpty()) {
            policySubJpaRepository.bulkSoftDelete(idsToUpdate);
        }

        // 서비스 로직에서 리턴값이 따로 필요 없다면 파라미터 그대로 리턴
        return savedEntities.stream()
                .map(PolicySubEntity::entityToDomain).toList();
    }
}
