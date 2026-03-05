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

    // MemberId와 현재 로그인한 Email로 여러 테이블 JOIN 해서 한 번에 조회
    // MemberDetailInfoDto로 바로 변환해야함 But, Builder는 JPQL에서 불가능
    // [To-Do] 1:N 관계(다중 회선) 발생 시 중복 행 문제 해결 필요
    // - Subscription: 현재는 1:1로 가정하나, 다중 회선 확장 시 최신/대표 회선 필터링 조건 추가 필요
    @Query("""
           SELECT DISTINCT new hotspot.user.member.infrastructure.entity.MemberDetailInfoDto(
               m, :email, s.phoneEnc, s.subId, fs.familyRole, fs.family.familyId
           )
           FROM MemberEntity m
           LEFT JOIN SubscriptionEntity s ON s.member = m
           LEFT JOIN FamilySubscriptionEntity fs ON fs.subscription = s
           WHERE m.id = :memberId
             AND EXISTS (
                 SELECT 1 FROM SocialAccountEntity sa
                 WHERE sa.member = m AND sa.email = :email
             )
           """)
    Optional<MemberDetailInfoDto> findDetailQueryResult(@Param("memberId") Long memberId, @Param("email") String email);
}
