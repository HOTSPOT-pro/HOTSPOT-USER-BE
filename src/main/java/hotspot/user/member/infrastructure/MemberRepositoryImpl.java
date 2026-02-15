package hotspot.user.member.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.infrastructure.entity.MemberEntity;
import hotspot.user.member.infrastructure.entity.SocialAccountEntity;
import hotspot.user.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;
    private final SocialAccountJpaRepository socialAccountJpaRepository;

    @Override
    @Transactional
    public Member save(Member member) {
        // 1. Member 저장
        MemberEntity memberEntity = MemberEntity.domainToEntity(member);
        MemberEntity savedMemberEntity = memberJpaRepository.save(memberEntity);

        // 2. SocialAccount 저장 (Member ID 연결)
        SocialAccount socialAccount = null;
        if (member.getSocialAccount() != null) {
            SocialAccountEntity socialAccountEntity = SocialAccountEntity.domainToEntity(
                    member.getSocialAccount(),
                    savedMemberEntity
            );
            SocialAccountEntity savedSocialAccountEntity = socialAccountJpaRepository.save(socialAccountEntity);
            socialAccount = savedSocialAccountEntity.entityToDomain();
        }

        // 3. 다시 조회하지 않고 저장된 엔티티들을 사용하여 직접 도메인 객체 조립 및 반환
        return savedMemberEntity.entityToDomain(socialAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(String email) {
        // 1. Fetch Join을 사용하여 한 번에 조회 (성능 최적화: N+1 방지)
        return socialAccountJpaRepository.findByEmailWithMember(email)
                .map(socialAccountEntity -> {
                    MemberEntity memberEntity = socialAccountEntity.getMember();
                    return memberEntity.entityToDomain(socialAccountEntity.entityToDomain());
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id)
                .map(memberEntity -> {
                    SocialAccount socialAccount = socialAccountJpaRepository.findByMemberId(id)
                            .map(SocialAccountEntity::entityToDomain)
                            .orElse(null);
                    return memberEntity.entityToDomain(socialAccount);
                });
    }
}
