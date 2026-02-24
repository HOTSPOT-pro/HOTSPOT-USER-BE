package hotspot.user.member.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.infrastructure.entity.MemberEntity;
import hotspot.user.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;
    private final PhoneDecryptor phoneDecryptor;

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
        // 쿼리 결과를 DTO로 받고, 안전하게 도메인 객체(MemberDetailInfo)로 변환
        return memberJpaRepository.findDetailQueryResult(id, email)
                .map(dto -> MemberDetailInfo.builder()
                        .member(dto.memberEntity().entityToDomain())
                        .email(dto.email())
                        .phone(phoneDecryptor.decrypt(dto.phone())) // 복호화 적용
                        .subId(dto.subId())
                        .role(dto.role())
                        .familyId(dto.familyId())
                        .build()
                );
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
