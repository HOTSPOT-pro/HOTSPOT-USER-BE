package hotspot.user.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.security.core.Authentication;

import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.auth.domain.Token;
import hotspot.user.auth.service.port.TokenRepository;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;

/**
 *  토큰 재발급 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class ReissueTokenServiceImplTest {

    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private ReissueTokenServiceImpl reissueTokenService;

    @Test
    @DisplayName("토큰 재발급 성공: 유효한 리프레시 토큰으로 새로운 토큰을 발급한다")
    void reissueSuccess() {
        // given
        String oldRefreshToken = "old-refresh-token";
        TokenRequest request = new TokenRequest(oldRefreshToken);
        Long memberId = 1L;

        PrincipalDetails principal = new PrincipalDetails(memberId, "test@test.com", 100L,
                FamilyRole.CHILD, Status.APPROVED);
        Token savedToken = Token.builder()
                .memberId(memberId)
                .refreshToken(oldRefreshToken)
                .build();

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(principal);

        given(jwtProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(jwtProvider.getAuthenticationFromRefreshToken(oldRefreshToken)).willReturn(authentication);

        given(tokenRepository.findByMemberId(memberId)).willReturn(Optional.of(savedToken));
        given(jwtProvider.createAccessToken(authentication)).willReturn("new-access-token");
        given(jwtProvider.createRefreshToken(authentication)).willReturn("new-refresh-token");

        // when
        TokenResponse response = reissueTokenService.reissue(request);

        // then
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    @DisplayName("토큰 재발급 실패: 유효하지 않은 토큰일 때")
    void reissueFailInvalidToken() {
        // given
        String invalidToken = "invalid-token";
        TokenRequest request = new TokenRequest(invalidToken);
        given(jwtProvider.validateToken(invalidToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reissueTokenService.reissue(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(AuthErrorCode.INVALID_TOKEN.getMessage());
    }

    @Test
    @DisplayName("토큰 재발급 실패: Redis에 저장된 토큰과 일치하지 않을 때")
    void reissueFailTokenNotMatch() {
        // given
        String refreshToken = "some-token";
        TokenRequest request = new TokenRequest(refreshToken);
        Long memberId = 1L;

        PrincipalDetails principal = new PrincipalDetails(memberId, "test@test.com", 100L,
                FamilyRole.CHILD, Status.APPROVED);
        Token savedToken = Token.builder()
                .memberId(memberId)
                .refreshToken("different-token")
                .build();

        given(jwtProvider.validateToken(refreshToken)).willReturn(true);

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(principal);

        given(jwtProvider.getAuthenticationFromRefreshToken(refreshToken)).willReturn(authentication);

        given(tokenRepository.findByMemberId(memberId)).willReturn(Optional.of(savedToken));

        // when & then
        assertThatThrownBy(() -> reissueTokenService.reissue(request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(AuthErrorCode.TOKEN_NOT_MATCH.getMessage());
    }
}
