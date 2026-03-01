package hotspot.user.policy.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.policy.infrastructure.entity.BlockPolicyEntity;

public interface BlockPolicyJpaRepository extends JpaRepository<BlockPolicyEntity, Long> {
    List<BlockPolicyEntity> findAllByIsActiveTrueAndFamilyIdIsNull();
    List<BlockPolicyEntity> findAllByFamilyId(Long familyId); // 우리 가족이 생성한 정책 조회

}
