package hotspot.user.presentData.infrastructure;

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
    List<GiftGiverRow> findGiftGivers(
            @Param("giftIds") List<Long> giftIds
    );
}
