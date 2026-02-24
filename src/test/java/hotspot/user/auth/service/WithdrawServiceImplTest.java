package hotspot.user.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.service.port.TokenRepository;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.Status;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.member.service.port.SocialAccountRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class WithdrawServiceImplTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SocialAccountRepository socialAccountRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private WithdrawServiceImpl withdrawService;

    @Test
    @DisplayName("회원 탈퇴 성공: 모든 연관 데이터가 정상적으로 처리된다")
    void withdrawSuccess() {
        // given
        Long memberId = 1L;
        String refreshToken = "valid-token";
        TokenRequest request = new TokenRequest(refreshToken);

        PrincipalDetails principal = new PrincipalDetails(
                memberId, "test@test.com", 100L, FamilyRole.PARENT, Status.APPROVED);
        Authentication authentication = Mockito.mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(principal);

        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getAuthenticationFromRefreshToken(refreshToken)).willReturn(authentication);

        Subscription subscription = Subscription.builder().id(10L).build();
        given(subscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(subscription));

        Member member = Member.builder().id(memberId).build();
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        withdrawService.withdraw(memberId, request);

        // then
        verify(tokenRepository).deleteByMemberId(memberId);
        verify(subscriptionRepository).save(any(Subscription.class)); // memberId=null 업데이트 확인
        verify(socialAccountRepository).deleteByMemberId(memberId);
        verify(memberRepository).delete(member);
    }

    @Test
    @DisplayName("회원 탈퇴 실패: 타인의 토큰으로 탈퇴 시도 시 예외 발생")
    void withdrawFailWrongOwner() {
        // given
        Long requesterId = 1L;
        Long ownerId = 2L;
        String refreshToken = "others-token";
        TokenRequest request = new TokenRequest(refreshToken);

        PrincipalDetails principal = new PrincipalDetails(
                ownerId, "other@test.com", 100L, FamilyRole.CHILD, Status.APPROVED);
        Authentication authentication = Mockito.mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(principal);

        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getAuthenticationFromRefreshToken(refreshToken)).willReturn(authentication);

        // when & then
        assertThatThrownBy(() -> withdrawService.withdraw(requesterId, request))
                .isInstanceOf(ApplicationException.class);

        verify(memberRepository, never()).delete(any());
    }
}
