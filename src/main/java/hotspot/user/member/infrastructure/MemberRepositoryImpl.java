package hotspot.user.member.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.infrastructure.entity.MemberDetailInfoDto;
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
    public Optional<MemberDetailInfo> findDetailByIdAndEmail(Long id, String email) {
        return memberJpaRepository.findDetailByIdAndEmail(id, email)
                .map(this::mapToDomain);
    }

    @Override
    public Optional<MemberDetailInfo> findDetailById(Long id) {
        return memberJpaRepository.findDetailById(id)
                .map(this::mapToDomain);
    }

    private MemberDetailInfo mapToDomain(MemberDetailInfoDto dto) {
        return MemberDetailInfo.builder()
                .member(dto.memberEntity().entityToDomain())
                .email(dto.email())
                .phone(dto.phone())
                .subId(dto.subId())
                .role(dto.role())
                .familyId(dto.familyId())
                .build();
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
