package hotspot.user.member.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.infrastructure.entity.MemberEntity;
import hotspot.user.member.infrastructure.entity.SocialAccountEntity;
import hotspot.user.member.service.port.SocialAccountRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SocialAccountRepositoryImpl implements SocialAccountRepository {

    private final SocialAccountJpaRepository socialAccountJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public SocialAccount save(SocialAccount socialAccount) {
        // SocialAccountEntity 생성을 위해 MemberEntity 조회가 필요함 (JPA 연관관계 때문)
        MemberEntity memberEntity = memberJpaRepository.findById(socialAccount.getMemberId())
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        return socialAccountJpaRepository.save(SocialAccountEntity.domainToEntity(socialAccount, memberEntity))
                .entityToDomain();
    }

    @Override
    public Optional<SocialAccount> findByMemberId(Long memberId) {
        return socialAccountJpaRepository.findByMemberId(memberId)
                .map(SocialAccountEntity::entityToDomain);
    }

    @Override
    public Optional<SocialAccount> findByEmail(String email) {
        return socialAccountJpaRepository.findByEmail(email)
                .map(SocialAccountEntity::entityToDomain);
    }
}
