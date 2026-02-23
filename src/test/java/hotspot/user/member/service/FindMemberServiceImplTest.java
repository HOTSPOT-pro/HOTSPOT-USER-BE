package hotspot.user.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.member.controller.response.MemberResponse;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.domain.Status;
import hotspot.user.member.service.port.MemberRepository;

/**
 * 회원 조회 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class FindMemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private FindMemberServiceImpl findMemberService;

    @Test
    @DisplayName("모든 정보(회원, 소셜, 회선, 가족)가 존재할 때 정상적으로 조회된다")
    void findByIdSuccessAllInfo() {
        // given
        Long memberId = 1L;
        String email = "test@email.com";
        Long subId = 10L;

        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .status(Status.APPROVED)
                .build();

        MemberDetailInfo detailInfo = MemberDetailInfo.builder()
                .member(member)
                .email(email)
                .phone("010-1234-5678")
                .subId(subId)
                .role(FamilyRole.PARENT)
                .familyId(100L)
                .build();

        given(memberRepository.findDetailByIdAndEmail(memberId, email)).willReturn(Optional.of(detailInfo));

        // when
        MemberResponse response = findMemberService.findById(memberId, email);

        // then
        assertThat(response.id()).isEqualTo(memberId);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.phone()).isEqualTo("010-1234-5678");
        assertThat(response.familyRole()).isEqualTo(FamilyRole.PARENT);
        assertThat(response.familyId()).isEqualTo(100L);
        assertThat(response.subId()).isEqualTo(subId);
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 예외가 발생한다")
    void findByIdFailMemberNotFound() {
        // given
        Long memberId = 999L;
        String email = "notfound@email.com";
        given(memberRepository.findDetailByIdAndEmail(memberId, email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findMemberService.findById(memberId, email))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
