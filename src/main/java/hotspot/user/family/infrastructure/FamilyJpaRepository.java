package hotspot.user.family.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.user.family.infrastructure.entity.FamilyDetailInfoDto;
import hotspot.user.family.infrastructure.entity.FamilyEntity;

/**
 * 가족 DB에 실제로 저장하는 JpaRepository
 */
public interface FamilyJpaRepository extends JpaRepository<FamilyEntity, Long> {

    @Query("""
           SELECT DISTINCT new hotspot.user.family.infrastructure.entity.FamilyDetailInfoDto(
               f, m, sa.email, s.phoneEnc, s.subId, fs.familyRole
           )
           FROM FamilyEntity f
           JOIN FamilySubscriptionEntity fs ON fs.family = f
           JOIN SubscriptionEntity s ON fs.subscription = s
           JOIN MemberEntity m ON s.member = m
           LEFT JOIN SocialAccountEntity sa ON sa.member = m
           WHERE f.familyId = :familyId
           """)
    List<FamilyDetailInfoDto> findFamilyDetailQueryResult(
            @Param("familyId") Long familyId);
}
