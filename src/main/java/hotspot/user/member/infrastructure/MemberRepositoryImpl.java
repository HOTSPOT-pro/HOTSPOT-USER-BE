package hotspot.user.member.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.member.domain.Member;
import hotspot.user.member.infrastructure.entity.MemberEntity;
import hotspot.user.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member save(Member member) {
        MemberEntity memberEntity = MemberEntity.domainToEntity(member);
        MemberEntity savedMemberEntity = memberJpaRepository.save(memberEntity);
        return savedMemberEntity.entityToDomain();
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id)
                .map(MemberEntity::entityToDomain);
    }

    @Override
    public void delete(Member member) {
        memberJpaRepository.delete(MemberEntity.domainToEntity(member));
    }

    @Override
    public void deleteById(Long id) {
        memberJpaRepository.deleteById(id);
    }
}
