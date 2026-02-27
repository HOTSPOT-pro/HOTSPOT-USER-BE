package hotspot.user.family.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.user.family.infrastructure.entity.FamilySubscriptionEntity;

/**
 * 가족-회선 매핑 JPA 저장소
 */
public interface FamilySubscriptionJpaRepository extends JpaRepository<FamilySubscriptionEntity, Long> {

    @EntityGraph(attributePaths = {"family", "subscription", "subscription.member"})
    Optional<FamilySubscriptionEntity> findBySubscriptionSubId(Long subId);

    @EntityGraph(attributePaths = {"family", "subscription", "subscription.member"})
    List<FamilySubscriptionEntity> findByFamilyFamilyId(Long familyId);

    @EntityGraph(attributePaths = {"family", "subscription", "subscription.member"})
    Optional<FamilySubscriptionEntity> findBySubscriptionMemberId(Long memberId);

    // Bulk Update 쿼리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FamilySubscriptionEntity fs SET fs.priority = :priority WHERE fs.subscription.subId = :subId")
    void updatePriority(@Param("subId") Long subId, @Param("priority") int priority);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE FamilySubscriptionEntity fs SET fs.dataLimit = :dataLimit WHERE fs.subscription.subId = :subId")
    void updateDataLimit(@Param("subId") Long subId, @Param("dataLimit") long dataLimit);

    @Query("""
             SELECT
                 f.familyId as familyId,
                 m.name as name,
                 s.isLocked as isLocked,
                 fs.dataLimit as dataLimit,
                 f.familyDataAmount as familyDataAmount
             FROM FamilySubscriptionEntity fs
             JOIN fs.subscription s
             JOIN s.member m
            JOIN fs.family f
            WHERE s.subId = :subId
        """)
    Optional<FamilySubDataLimitRow> findDataLimitBySubId(@Param("subId") Long subId);

    /**
     * 조회 전용 Projection 인터페이스
     */
    interface FamilySubDataLimitRow {
        Long getFamilyId();
        String getName();
        Boolean getIsLocked();
        Long getDataLimit();
        Long getFamilyDataAmount();
    }

}
