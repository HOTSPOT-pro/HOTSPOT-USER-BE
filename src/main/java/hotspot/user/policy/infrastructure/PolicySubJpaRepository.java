package hotspot.user.policy.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.user.policy.infrastructure.entity.PolicySubEntity;

public interface PolicySubJpaRepository extends JpaRepository<PolicySubEntity, Long> {
    List<PolicySubEntity> findBySubscriptionSubId(Long subId);

    // 현재 활성화된 정책만 조회
    List<PolicySubEntity> findBySubscriptionSubIdAndIsActiveTrue(Long subId);

    // 정책에 해당되있는 회선 ID 목록 조회
    @Query("""
       SELECT p.subscription.subId
       FROM PolicySubEntity p
       WHERE p.blockPolicy.blockPolicyId = :blockPolicyId
       AND p.isActive = true
       """)
    List<Long> findActiveSubIdsByBlockPolicyId(@Param("blockPolicyId") Long blockPolicyId);

    // N+1 SELECT를 방지하기 위한 벌크 UPDATE 쿼리 (비활성화, isActive = false)
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE PolicySubEntity p SET p.isActive = false, p.modifiedTime = CURRENT_TIMESTAMP
            WHERE p.policySubId IN :ids
            """)
    void bulkDeActive(@Param("ids") List<Long> ids);

    // N+1 SELECT를 방지하기 위한 벌크 UPDATE 쿼리 (활성화, isActive = true)
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE PolicySubEntity p SET p.isActive = true, p.modifiedTime = CURRENT_TIMESTAMP
            WHERE p.policySubId IN :ids
            """)
    void bulkActivate(@Param("ids") List<Long> ids);

    // 정책(BlockPolicy)이 비활성화될 때 해당 정책을 적용 중인 모든 회선 매핑 정보를 비활성화 처리
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE PolicySubEntity p SET p.isActive = false, p.modifiedTime = CURRENT_TIMESTAMP
            WHERE p.blockPolicy.blockPolicyId
            IN :blockPolicyIds
            """)
    void bulkDeActiveByBlockPolicyIds(@Param("blockPolicyIds") List<Long> blockPolicyIds);

    @Query("""
        SELECT ps.blockPolicyId, ps.subId
        FROM PolicySubEntity ps
        WHERE ps.blockPolicyId IN :policyIds
        AND ps.isActive = true
        """)
    List<Object[]> findActiveSubIdsByBlockPolicyIds(List<Long> policyIds);
}
