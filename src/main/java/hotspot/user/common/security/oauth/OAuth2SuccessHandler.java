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
        // setMaxAge는 초 단위이므로 ms를 1000으로 나눔
        ResponseCookie refreshCookie = CookieUtil.createCookie("refreshToken", refreshToken, refreshExpiration / 1000);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // Access Token만 Query Parameter로 전달하여 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
