package hotspot.user.member.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.domain.Status;
import hotspot.user.member.infrastructure.entity.MemberDetailInfoDto;
import hotspot.user.member.infrastructure.entity.MemberEntity;

/**
 * 회원 Repository 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class MemberRepositoryImplTest {

    @Mock
    private MemberJpaRepository memberJpaRepository;

    @InjectMocks
    private MemberRepositoryImpl memberRepository;

    @Test
    @DisplayName("멤버 저장 성공: 도메인 객체를 엔티티로 변환하여 저장하고 다시 도메인으로 반환한다")
    void saveMemberSuccess() {
        // given
        Member member = Member.builder().name("홍길동").status(Status.PENDING).build();
        MemberEntity entity = MemberEntity.builder().id(1L).name("홍길동").status(Status.PENDING).build();
        given(memberJpaRepository.save(any(MemberEntity.class))).willReturn(entity);

        // when
        Member savedMember = memberRepository.save(member);

        // then
        assertThat(savedMember.getId()).isEqualTo(1L);
        assertThat(savedMember.getName()).isEqualTo("홍길동");
        verify(memberJpaRepository).save(any(MemberEntity.class));
    }

    @Test
    @DisplayName("멤버 ID 조회 성공")
    void findByIdSuccess() {
        // given
        Long memberId = 1L;
        MemberEntity entity = MemberEntity.builder().id(memberId).name("홍길동").status(Status.APPROVED).build();
        given(memberJpaRepository.findById(memberId)).willReturn(Optional.of(entity));

        // when
        Optional<Member> result = memberRepository.findById(memberId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("멤버 상세 정보 조회 성공: JOIN 쿼리 결과를 도메인 객체로 변환한다")
    void findDetailByIdSuccess() {
        // given
        Long memberId = 1L;
        String email = "test@email.com";
        MemberEntity entity = MemberEntity.builder().id(memberId).name("홍길동").status(Status.APPROVED).build();
        MemberDetailInfoDto dto = MemberDetailInfoDto.builder()
                .memberEntity(entity)
                .email(email)
                .phone("010-1234-5678")
                .subId(10L)
                .role(FamilyRole.OWNER)
                .familyId(100L)
                .build();

        given(memberJpaRepository.findDetailByIdAndEmail(memberId, email)).willReturn(Optional.of(dto));

        // when
        Optional<MemberDetailInfo> result = memberRepository.findDetailByIdAndEmail(memberId, email);

        // then
        assertThat(result).isPresent();
        MemberDetailInfo info = result.get();
        assertThat(info.getMember().getId()).isEqualTo(memberId);
        assertThat(info.getEmail()).isEqualTo(email);
        assertThat(info.getPhone()).isEqualTo("010-1234-5678");
        assertThat(info.getRole()).isEqualTo(FamilyRole.OWNER);
        assertThat(info.getFamilyId()).isEqualTo(100L);
        assertThat(info.getSubId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("멤버 상세 정보 조회 실패: 존재하지 않는 회원")
    void findDetailByIdNotFound() {
        // given
        Long memberId = 999L;
        String email = "notfound@email.com";
        given(memberJpaRepository.findDetailByIdAndEmail(memberId, email)).willReturn(Optional.empty());

        // when
        Optional<MemberDetailInfo> result = memberRepository.findDetailByIdAndEmail(memberId, email);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("멤버 삭제 성공")
    void deleteMemberSuccess() {
        // given
        Member member = Member.builder().id(1L).build();

        // when
        memberRepository.delete(member);

        // then
        verify(memberJpaRepository).delete(any(MemberEntity.class));
    }

    @Test
    @DisplayName("멤버 ID로 삭제 성공")
    void deleteByIdSuccess() {
        // given
        Long memberId = 1L;

        // when
        memberRepository.deleteById(memberId);

        // then
        verify(memberJpaRepository).deleteById(memberId);
    }
}
