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
                   f, m, (
                       SELECT sa.email FROM SocialAccountEntity sa 
                       WHERE sa.id = (
                           SELECT MAX(sa2.id) FROM SocialAccountEntity sa2 
                           WHERE sa2.member = m AND sa2.isDeleted = false
                       )
                   ), s.phoneEnc, s.subId, fs.familyRole
               )
               FROM FamilySubscriptionEntity fs
               JOIN fs.family f
               JOIN fs.subscription s
               JOIN s.member m
               WHERE f.familyId = :familyId
               """)
        List<FamilyDetailInfoDto> findFamilyDetailQueryResult(            @Param("familyId") Long familyId);
}
