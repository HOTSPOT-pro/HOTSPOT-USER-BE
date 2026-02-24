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

        // 사용자 상태에 따라 처리 로직 분기
        String targetUrl;

        // 온보딩으로 리다이렉트 (임시 토큰 발급)
        if (principal.getStatus() == Status.PENDING) {
            log.info("신규 사용자, 온보딩 페이지로 리다이렉트: memberId={}", principal.getId());
            String accessToken = jwtProvider.createOnboardingToken(authentication);
            targetUrl = determineOnboardingUrl(accessToken);
        }

        // 바로 로그인 (토큰 발급 O)
        else {
            log.info("기존 사용자, 메인 페이지로 리다이렉트: memberId={}", principal.getId());

            // JWT 토큰 생성 및 저장
            String accessToken = jwtProvider.createAccessToken(authentication);
            String refreshToken = jwtProvider.createRefreshToken(authentication);
            TokenRequest tokenRequest = new TokenRequest(refreshToken);
            saveTokenService.saveToken(principal.getId(), tokenRequest);

            // Refresh Token을 HttpOnly Cookie에 저장
            ResponseCookie refreshCookie = CookieUtil.createCookie("refreshToken", refreshToken, refreshExpiration);
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            targetUrl = determineMainUrl(accessToken);
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String determineOnboardingUrl(String accessToken) {
        String baseUri = redirectUri + "/" + onboardingRedirectUri;
        return UriComponentsBuilder.fromUriString(baseUri)
                .queryParam("accessToken", accessToken)
                .build().toUriString();
    }

    private String determineMainUrl(String accessToken) {
        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .build().toUriString();
    }
}
