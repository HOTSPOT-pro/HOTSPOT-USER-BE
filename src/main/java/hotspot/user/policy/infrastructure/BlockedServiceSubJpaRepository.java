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

    // is_deleted = true인 데이터도 포함해서 조회 -> Upsert 위해서
    // nativeQuery=true 해서 @Where부분 우회
    @Query(value = """
        SELECT * FROM blocked_service_sub b
        WHERE b.sub_id = :subId
        AND b.blocked_service_id IN (:serviceIds)
        """, nativeQuery = true)
    List<BlockedServiceSubEntity> findBySubIdAndServiceIdsIncludeDeleted(
            @Param("subId") Long subId, @Param("serviceIds") Set<Long> serviceIds);


    // 여러 개의 서비스 차단을 한 번에 해제
    // clearAutomatically = true => DB 직접 수정했으니 영속성 컨텍스트 비우라는 의미
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE BlockedServiceSubEntity b
            SET b.isDeleted = true
            WHERE b.subscription.subId = :subId
            AND b.appBlockedService.appBlockedServiceId IN :serviceIds
            """)
    void bulkSoftDelete(@Param("subId") Long subId, @Param("serviceIds") Set<Long> serviceIds);
}
