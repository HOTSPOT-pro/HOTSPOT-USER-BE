package hotspot.user.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
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
import hotspot.user.dispatch.sse.registry.SseEmitterRegistry;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

/**
 * 로그아웃 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class LogoutServiceImplTest {

    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private SseEmitterRegistry sseEmitterRegistry;

    @InjectMocks
    private LogoutServiceImpl logoutService;

    @Test
    @DisplayName("로그아웃 성공: 유효한 토큰 및 본인 소유일 때 토큰을 삭제한다")
    void logoutSuccess() {
        // given
        String refreshToken = "valid-token";
        TokenRequest request = new TokenRequest(refreshToken);
        Long memberId = 1L;

        PrincipalDetails principal = new PrincipalDetails(memberId, "test@test.com", 100L,
                FamilyRole.CHILD, Status.APPROVED);

        Authentication authentication = Mockito.mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(principal);

        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getAuthenticationFromRefreshToken(refreshToken)).willReturn(authentication);
        given(subscriptionRepository.findByMemberId(memberId))
                .willReturn(Optional.of(Subscription.builder().id(1000003L).build()));

        // when
        logoutService.logout(memberId, request);

        // then
        verify(tokenRepository).deleteByMemberId(memberId);
        verify(sseEmitterRegistry).closeAllBySubId(1000003L);
    }

    @Test
    @DisplayName("로그아웃 실패: 유효하지 않은 토큰일 때 예외를 발생시킨다")
    void logoutWithInvalidToken() {
        // given
        String invalidToken = "invalid-token";
        TokenRequest request = new TokenRequest(invalidToken);
        Long memberId = 1L;
        given(jwtProvider.validateToken(invalidToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> logoutService.logout(memberId, request))
                .isInstanceOf(ApplicationException.class);

        verify(tokenRepository, Mockito.never()).deleteByMemberId(Mockito.anyLong());
    }

    @Test
    @DisplayName("로그아웃 실패: 토큰 소유자가 요청자와 다를 때 예외를 발생시킨다")
    void logoutWithWrongOwner() {
        // given
        String refreshToken = "others-token";
        TokenRequest request = new TokenRequest(refreshToken);
        Long requesterId = 1L;
        Long ownerId = 2L;

        PrincipalDetails principal = new PrincipalDetails(ownerId, "other@test.com", 100L,
                FamilyRole.CHILD, Status.APPROVED);

        Authentication authentication = Mockito.mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(principal);

        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getAuthenticationFromRefreshToken(refreshToken)).willReturn(authentication);

        // when & then
        assertThatThrownBy(() -> logoutService.logout(requesterId, request))
                .isInstanceOf(ApplicationException.class);

        verify(tokenRepository, Mockito.never()).deleteByMemberId(Mockito.anyLong());
    }
}
