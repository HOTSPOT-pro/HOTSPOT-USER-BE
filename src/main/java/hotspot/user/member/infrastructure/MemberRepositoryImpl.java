package hotspot.user.member.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.member.domain.Member;
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
        if (member.getSocialAccount() != null) {
            SocialAccountEntity socialAccountEntity = SocialAccountEntity.domainToEntity(
                    member.getSocialAccount(),
                    savedMemberEntity
            );
            socialAccountJpaRepository.save(socialAccountEntity);
        }

        // 3. 다시 도메인으로 변환하여 반환
        return findById(savedMemberEntity.getId()).orElseThrow();
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

    // 내부적으로 사용하거나 추후 인터페이스에 추가될 수 있는 메서드
    public Optional<Member> findById(Long id) {
        Optional<MemberEntity> memberEntityOpt = memberJpaRepository.findById(id);

        if (memberEntityOpt.isEmpty()) {
            return Optional.empty();
        }

        MemberEntity memberEntity = memberEntityOpt.get();

        Optional<SocialAccountEntity> socialAccountOpt = socialAccountJpaRepository.findByMemberId(id);

        if (socialAccountOpt.isEmpty()) {
            return Optional.of(memberEntity.entityToDomain());
        }

        return Optional.of(memberEntity.entityToDomain(socialAccountOpt.get().entityToDomain()));
    }
}
