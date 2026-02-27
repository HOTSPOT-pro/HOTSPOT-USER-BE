package hotspot.user.presentData.infrastructure;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.user.presentData.infrastructure.entity.PresentDataEntity;

public interface PresentDataJpaRepository extends JpaRepository<PresentDataEntity, Long> {

    interface GiftGiverRow {
        Long getGiftId();
        String getGiverName();
    }

    @Query("""
        select
            pd.presentDataId as giftId,
            m.name as giverName
        from PresentDataEntity pd
            join pd.provideSubscription ps
            join ps.member m
        where pd.presentDataId in :giftIds
    """)
    List<GiftGiverRow> findGiftGivers(@Param("giftIds") List<Long> giftIds);

    @Query("""
        select p
        from PresentDataEntity p
            join fetch p.provideSubscription ps
            join fetch ps.member
        where p.targetSubscription.subId = :subId
        order by p.createdTime desc
    """)
    List<PresentDataEntity> findAllByTargetSubId(@Param("subId") Long subId);

    @Query("""
        select p
        from PresentDataEntity p
            join fetch p.targetSubscription ts
            join fetch ts.member
        where p.provideSubscription.subId = :subId
        order by p.createdTime desc
    """)
    List<PresentDataEntity> findAllByProviderSubId(@Param("subId") Long subId);

    @Query("""
        select coalesce(sum(p.dataAmount), 0)
        from PresentDataEntity p
        where p.provideSubscription.subId = :providerSubId
          and p.createdTime >= :start
          and p.createdTime < :end
    """)
    long sumMonthlySentKb(
            @Param("providerSubId") Long providerSubId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
