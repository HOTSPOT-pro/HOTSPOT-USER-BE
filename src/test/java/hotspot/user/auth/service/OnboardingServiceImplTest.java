package hotspot.user.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.auth.controller.port.IssueTokenService;
import hotspot.user.auth.controller.request.OnboardingRequest;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.common.util.PhoneUtil;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.domain.Status;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.member.service.port.SocialAccountRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

/**
 * 온보딩 서비스 단위 테스트 코드
 */
@ExtendWith(MockitoExtension.class)
class OnboardingServiceImplTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SocialAccountRepository socialAccountRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;
    @Mock
    private IssueTokenService issueTokenService;

    @InjectMocks
    private OnboardingServiceImpl onboardingService;

    @Test
    @DisplayName("신규 온보딩 성공: 회선이 있고 가족 결합 정보가 존재할 때 승인 절차를 거쳐 토큰을 발급한다")
    void onboardingSuccessNewMember() {
        // given
        Long memberId = 1L;
        Long familyId = 100L;
        String phoneNumber = "01012345678";
        String phoneHash = PhoneUtil.hashPhoneNumber(phoneNumber);
        OnboardingRequest request = new OnboardingRequest(memberId, "test@test.com", phoneNumber, "950101");

        Member pendingMember = Member.builder().id(memberId).name("test").status(Status.PENDING).build();
        SocialAccount socialAccount = SocialAccount.builder().memberId(memberId).email("test@test.com").build();
        Subscription subscription = Subscription.builder().id(100L).phoneHash(phoneHash).build();
        Family family = Family.builder().id(familyId).build();
        FamilySubscription familySubscription = FamilySubscription.builder()
                .family(family).familyRole(FamilyRole.CHILD).build();

        given(subscriptionRepository.findByPhoneHash(phoneHash)).willReturn(Optional.of(subscription));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(pendingMember));
        given(socialAccountRepository.findByMemberId(memberId)).willReturn(Optional.of(socialAccount));
        given(familySubscriptionRepository.findBySubId(100L)).willReturn(Optional.of(familySubscription));

        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(subscriptionRepository.save(any(Subscription.class))).willAnswer(invocation -> invocation.getArgument(0));

        TokenResponse expectedResponse = new TokenResponse("at", "rt");
        given(issueTokenService.issue(any(Member.class), eq("test@test.com"), eq(FamilyRole.CHILD), eq(familyId)))
                .willReturn(expectedResponse);

        // when
        TokenResponse response = onboardingService.onboarding(request);

        // then
        assertThat(response.accessToken()).isEqualTo("at");
        verify(issueTokenService).issue(any(Member.class), anyString(), any(FamilyRole.class), anyLong());
    }

    @Test
    @DisplayName("기존 회원 통합 성공: 회선에 이미 가입된 회원이 있을 때 계정을 합치고 토큰을 발급한다")
    void onboardingSuccessExistingMemberMerge() {
        // given
        Long pendingMemberId = 1L;
        Long existingMemberId = 2L;
        Long familyId = 100L;
        String phoneNumber = "01012345678";
        String phoneHash = PhoneUtil.hashPhoneNumber(phoneNumber);
        OnboardingRequest request = new OnboardingRequest(pendingMemberId, "test@test.com", phoneNumber, "950101");

        Member pendingMember = Member.builder().id(pendingMemberId).status(Status.PENDING).build();
        Member existingMember = Member.builder().id(existingMemberId).status(Status.APPROVED).build();
        SocialAccount socialAccount = SocialAccount.builder().memberId(pendingMemberId).email("test@test.com").build();
        Subscription subscription = Subscription.builder().id(100L).member(existingMember).phoneHash(phoneHash).build();
        Family family = Family.builder().id(familyId).build();
        FamilySubscription familySubscription = FamilySubscription.builder()
                .family(family).familyRole(FamilyRole.PARENT).build();

        given(subscriptionRepository.findByPhoneHash(phoneHash)).willReturn(Optional.of(subscription));
        given(memberRepository.findById(pendingMemberId)).willReturn(Optional.of(pendingMember));
        given(socialAccountRepository.findByMemberId(pendingMemberId)).willReturn(Optional.of(socialAccount));
        given(familySubscriptionRepository.findBySubId(100L)).willReturn(Optional.of(familySubscription));

        given(socialAccountRepository.save(any(SocialAccount.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        TokenResponse expectedResponse = new TokenResponse("at", "rt");
        given(issueTokenService.issue(any(Member.class), eq("test@test.com"), eq(FamilyRole.PARENT), eq(familyId)))
                .willReturn(expectedResponse);

        // when
        TokenResponse response = onboardingService.onboarding(request);

        // then
        assertThat(response.accessToken()).isEqualTo("at");
        verify(memberRepository).delete(pendingMember);
        verify(issueTokenService).issue(any(Member.class), anyString(), any(FamilyRole.class), anyLong());
    }

    @Test
    @DisplayName("온보딩 실패: 해당 전화번호로 회선 정보를 찾을 수 없을 때")
    void onboardingFailSubscriptionNotFound() {
        // given
        String phoneNumber = "01000000000";
        String phoneHash = PhoneUtil.hashPhoneNumber(phoneNumber);
        OnboardingRequest request = new OnboardingRequest(1L, "test@test.com", phoneNumber, "950101");

        given(subscriptionRepository.findByPhoneHash(phoneHash)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> onboardingService.onboarding(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(MemberErrorCode.SUBSCRIPTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("온보딩 실패: 회선은 존재하지만 가족 결합 정보가 없는 경우")
    void onboardingFailFamilySubscriptionNotFound() {
        // given
        Long memberId = 1L;
        String phoneNumber = "01012345678";
        String phoneHash = PhoneUtil.hashPhoneNumber(phoneNumber);
        OnboardingRequest request = new OnboardingRequest(memberId, "test@test.com", phoneNumber, "950101");

        Member pendingMember = Member.builder().id(memberId).status(Status.PENDING).build();
        Subscription subscription = Subscription.builder().id(100L).phoneHash(phoneHash).build();
        SocialAccount socialAccount = SocialAccount.builder().memberId(memberId).build();

        given(subscriptionRepository.findByPhoneHash(phoneHash)).willReturn(Optional.of(subscription));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(pendingMember));
        given(socialAccountRepository.findByMemberId(memberId)).willReturn(Optional.of(socialAccount));
        given(familySubscriptionRepository.findBySubId(100L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> onboardingService.onboarding(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(MemberErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND.getMessage());
    }
}
