package hotspot.user.common.security.oauth;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import hotspot.user.auth.controller.port.SaveTokenService;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.common.util.CookieUtil;
import hotspot.user.member.domain.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 성공적으로 끝나면 호출되는 Handler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final SaveTokenService saveTokenService;

    @Value("${server.domain.local}")
    private String redirectUri;

    @Value("${server.domain.onboarding}")
    private String onboardingRedirectUri;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        log.info("OAuth2 Login Success: {}", authentication.getName());

        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();

        // JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(authentication);

        TokenRequest tokenRequest = new TokenRequest(refreshToken);

        // redis에 (memberId, refreshToken) 저장
        saveTokenService.saveToken(principal.getId(), tokenRequest);

        // Refresh Token을 HttpOnly Cookie에 저장 (CookieUtil 사용)
        ResponseCookie refreshCookie = CookieUtil.createCookie("refreshToken", refreshToken, refreshExpiration);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 사용자 상태에 따라 리다이렉트 URI 결정
        String targetUrl = determineTargetUrl(principal, accessToken);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String determineTargetUrl(PrincipalDetails principal, String accessToken) {
        String baseUri;
        if (principal.getStatus() == Status.PENDING) {
            log.info("신규 사용자, 온보딩 페이지로 리다이렉트: memberId={}", principal.getId());
            baseUri = redirectUri + "/" + onboardingRedirectUri;
        } else {
            log.info("기존 사용자, 메인 페이지로 리다이렉트: memberId={}", principal.getId());
            baseUri = redirectUri;
        }

        return UriComponentsBuilder.fromUriString(baseUri)
                .queryParam("accessToken", accessToken)
                .build().toUriString();
    }
}
