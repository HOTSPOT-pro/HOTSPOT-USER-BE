package hotspot.user.policy.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.user.policy.infrastructure.entity.PolicySubEntity;

public interface PolicySubJpaRepository extends JpaRepository<PolicySubEntity, Long> {
    List<PolicySubEntity> findBySubscriptionSubId(Long subId);

    // N+1 SELECT를 방지하기 위한 벌크 UPDATE 쿼리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE PolicySubEntity p SET p.isDeleted = true WHERE p.policySubId IN :ids")
    void bulkSoftDelete(@Param("ids") List<Long> ids);
}
