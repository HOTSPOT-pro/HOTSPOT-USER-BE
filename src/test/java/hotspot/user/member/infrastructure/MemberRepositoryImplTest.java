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

import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.Status;
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
