package hotspot.user.common.security.oauth;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * 소셜 로그인 실패 시 호출 & 에러 메시지를 쿼리 파라미터에 담아 프론트에 리다이렉트
 */
@Slf4j
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${client.login-uri:http://localhost:3000/login}")
    private String loginPageUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 Login Failed: {}", exception.getMessage());

        // 실패 원인을 쿼리 파라미터(error)에 담아서 프론트엔드 로그인 페이지로 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString(loginPageUri)
                .queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
