package hotspot.user.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.auth.controller.response.LoginResponse;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.Provider;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.member.service.port.SocialAccountRepository;

@ExtendWith(MockitoExtension.class)
class RegisterSocialMemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SocialAccountRepository socialAccountRepository;

    @InjectMocks
    private RegisterSocialMemberServiceImpl registerSocialMemberService;

    @Test
    @DisplayName("소셜 회원가입 성공: 신규 멤버와 소셜 계정을 저장하고 로그인 응답을 반환한다")
    void registerSuccess() {
        // given
        CreateSocialAccountRequest request = new CreateSocialAccountRequest(
                "홍길동", "test@test.com", "social-id", Provider.KAKAO, null
        );
        Long memberId = 1L;

        Member savedMember = Member.builder()
                .id(memberId)
                .name("홍길동")
                .build();

        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(socialAccountRepository.save(any(SocialAccount.class))).willReturn(any(SocialAccount.class));

        // when
        LoginResponse response = registerSocialMemberService.register(request);

        // then
        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.email()).isEqualTo("test@test.com");
        verify(memberRepository).save(any(Member.class));
        verify(socialAccountRepository).save(any(SocialAccount.class));
    }
}
