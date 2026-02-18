package hotspot.user.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.auth.controller.response.LoginResponse;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.controller.port.RegisterSocialMemberService;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.Provider;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.domain.Status;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.member.service.port.SocialAccountRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

/**
 * 소셜 로그인 서비스 단위 테스트 코드
 */
@ExtendWith(MockitoExtension.class)
class SocialLoginServiceImplTest {

    @Mock
    private SocialAccountRepository socialAccountRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RegisterSocialMemberService registerSocialMemberService;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;


    @InjectMocks
    private SocialLoginServiceImpl socialLoginService;

    @Test
    @DisplayName("기존 회원이 로그인하면 LoginResultDto 정보를 반환한다")
    void loginSuccessExistingUser() {
        // given
        String email = "test@kakao.com";
        Long memberId = 1L;
        CreateSocialAccountRequest request = new CreateSocialAccountRequest(
                "홍길동",
                email,
                "socialId",
                Provider.KAKAO,
                null);

        SocialAccount socialAccount = SocialAccount.builder()
                .memberId(memberId)
                .email(email) // Added email to mock SocialAccount
                .build();

        Member member = Member.builder()
                .id(memberId)
                .name("홍길동")
                .status(Status.APPROVED)
                .build();

        Subscription subscription = Subscription.builder()
                .id(10L)
                .member(Member.builder().id(memberId).build()) // Corrected: use member() builder method
                .build();

        FamilySubscription familySubscription = FamilySubscription.builder()
                .id(100L)
                .subscription(subscription) // Corrected: use subscription() builder method
                .familyRole(FamilyRole.PARENT)
                .build();

        given(socialAccountRepository.findByEmail(email)).willReturn(Optional.of(socialAccount));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(subscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(subscription));
        given(familySubscriptionRepository.findBySubId(subscription.getId()))
                .willReturn(Optional.of(familySubscription));

        // when
        LoginResponse result = socialLoginService.login(request);

        // then
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.status()).isEqualTo(Status.APPROVED);
        assertThat(result.familyRole()).isEqualTo(FamilyRole.PARENT);
    }

    @Test
    @DisplayName("신규 회원이면 회원가입 서비스(register)를 호출하고 LoginResultDto를 반환한다")
    void loginSuccessNewUser() {
        // given
        String email = "new@kakao.com";
        CreateSocialAccountRequest request = new CreateSocialAccountRequest("신규", email, "newId", Provider.KAKAO, null);

        LoginResponse newLoginResult = new LoginResponse(
            2L,
            email,
            Status.PENDING,
            FamilyRole.CHILD
        );

        given(socialAccountRepository.findByEmail(email)).willReturn(Optional.empty());
        given(registerSocialMemberService.register(request)).willReturn(newLoginResult);

        // when
        LoginResponse result = socialLoginService.login(request);

        // then
        assertThat(result.memberId())
            .isEqualTo(newLoginResult.memberId());
        assertThat(result.email()).isEqualTo(newLoginResult.email());
        assertThat(result.status()).isEqualTo(newLoginResult.status());
        assertThat(result.familyRole()).isEqualTo(newLoginResult.familyRole());
        verify(registerSocialMemberService).register(request);
    }

    @Test
    @DisplayName("소셜 계정은 있는데 연결된 회원 정보가 없으면 예외가 발생한다")
    void loginFailSocialAccountExistsButMemberNotFound() {
        // given
        String email = "ghost@kakao.com";
        Long ghostMemberId = 999L;
        CreateSocialAccountRequest request = new CreateSocialAccountRequest(
                "유령",
                email,
                "ghostId",
                Provider.KAKAO,
                null);

        SocialAccount socialAccount = SocialAccount.builder()
                .memberId(ghostMemberId)
                .build();

        given(socialAccountRepository.findByEmail(email)).willReturn(Optional.of(socialAccount));
        given(memberRepository.findById(ghostMemberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> socialLoginService.login(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
