package hotspot.user.policy.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.user.policy.infrastructure.entity.BlockPolicyEntity;

public interface BlockPolicyJpaRepository extends JpaRepository<BlockPolicyEntity, Long> {
    List<BlockPolicyEntity> findAllByIsActiveTrueAndFamilyIdIsNull();
    List<BlockPolicyEntity> findAllByFamilyId(Long familyId); // 우리 가족이 생성한 정책 조회
    Optional<BlockPolicyEntity> findByBlockPolicyId(Long blockPolicyId); // 단일 정책 조회

    // N+1 SELECT를 방지하기 위한 벌크 UPDATE 쿼리 (비활성화, isActive = false)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE BlockPolicyEntity b SET b.isActive = false WHERE b.blockPolicyId IN :ids")
    void bulkDeActive(@Param("ids") List<Long> ids);

    // N+1 SELECT를 방지하기 위한 벌크 UPDATE 쿼리 (활성화, isActive = true)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE BlockPolicyEntity b SET b.isActive = true WHERE b.blockPolicyId IN :ids")
    void bulkActivate(@Param("ids") List<Long> ids);

    // N+1 SELECT를 방지하기 위한 벌크 Delete 쿼리 (isDeleted = true)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE BlockPolicyEntity b SET b.isDeleted = true, b.isActive = false WHERE b.blockPolicyId IN :ids")
    void bulkDelete(@Param("ids") List<Long> ids);
}
