package hotspot.user.policy.infrastructure;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.user.policy.infrastructure.entity.BlockedServiceSubEntity;

public interface BlockedServiceSubJpaRepository extends JpaRepository<BlockedServiceSubEntity, Long> {
    List<BlockedServiceSubEntity> findBySubscriptionSubId(Long subId);

    List<BlockedServiceSubEntity> findBySubscriptionSubIdAndIsActiveTrue(Long subId);

    // is_Active = false인 데이터도 포함해서 조회 -> Upsert 위해서
    @Query("""
        SELECT b FROM BlockedServiceSubEntity b
        WHERE b.subscription.subId = :subId
        AND b.appBlockedService.appBlockedServiceId IN :serviceIds
        """)
    List<BlockedServiceSubEntity> findBySubIdAndServiceIds(
            @Param("subId") Long subId, @Param("serviceIds") Set<Long> serviceIds);


    // 앱 차단 서비스 구독 목록 비활성화
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE BlockedServiceSubEntity b
            SET b.isActive = false, b.modifiedTime = CURRENT_TIMESTAMP
            WHERE b.blockedServiceSubId IN :ids
            """)
    void bulkDeactive(@Param("ids") List<Long> ids);

    // 앱 차단 서비스 구독 목록 활성화
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE BlockedServiceSubEntity b
            SET b.isActive = true, b.modifiedTime = CURRENT_TIMESTAMP
            WHERE b.blockedServiceSubId IN :ids
            """)
    void bulkActivate(@Param("ids") List<Long> ids);

    // 활성화된 차단 서비스 ID 리스트만 조회 (Entity mapping에 의한 NPE 방지)
    @Query("""
            SELECT b.appBlockedService.appBlockedServiceId
            FROM BlockedServiceSubEntity b
            WHERE b.subscription.subId = :subId
            AND b.isActive = true
            """)
    List<Long> findActiveServiceIdsBySubId(@Param("subId") Long subId);
}
