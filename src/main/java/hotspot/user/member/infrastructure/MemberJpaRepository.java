package hotspot.user.member.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hotspot.user.member.infrastructure.entity.MemberDetailInfoDto;
import hotspot.user.member.infrastructure.entity.MemberEntity;


@Repository
public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {

    // MemberId로 여러 테이블 JOIN 해서 한 번에 조회
    // MemberDetailInfoDto로 바로 변환해야함 But, Builder는 JPQL에서 불가능
    @Query("""
           SELECT new hotspot.user.member.infrastructure.entity.MemberDetailInfoDto(
               m, sa.email, s.phoneEnc, s.subId, fs.familyRole, fs.family.familyId
           )
           FROM MemberEntity m
           LEFT JOIN SocialAccountEntity sa ON sa.member = m
           LEFT JOIN SubscriptionEntity s ON s.member = m
           LEFT JOIN FamilySubscriptionEntity fs ON fs.subscription = s
           WHERE m.id = :memberId
           """)
    Optional<MemberDetailInfoDto> findDetailQueryResult(@Param("memberId") Long memberId);
}
