package hotspot.user.member.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hotspot.user.member.infrastructure.entity.SocialAccountEntity;

@Repository
public interface SocialAccountJpaRepository extends JpaRepository<SocialAccountEntity, Long> {
    Optional<SocialAccountEntity> findByEmail(String email);

    @Query("SELECT sa FROM SocialAccountEntity sa JOIN FETCH sa.member WHERE sa.email = :email")
    Optional<SocialAccountEntity> findByEmailWithMember(@Param("email") String email);

    Optional<SocialAccountEntity> findByMemberId(Long memberId);
}
