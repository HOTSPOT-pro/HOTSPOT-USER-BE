package hotspot.user.common.security.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import hotspot.user.common.exception.code.AuthErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 모든 요청에서 JWT 토큰을 검사하여 유효할 경우 SecurityContext에 인증 정보를 저장하는 필터
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Request Header에서 토큰 추출
        String requestURI = request.getRequestURI();

        try {
            String token = jwtProvider.resolveToken(request);

            if (StringUtils.hasText(token)) {
                // 토큰 검증 - 실패 시 예외 발생
                jwtProvider.validateToken(token);

                // 검증 성공 시 인증 객체 저장
                Authentication authentication = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) { // 만료된 토큰
            request.setAttribute("exception", AuthErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) { // 이상한 토큰
            request.setAttribute("exception", AuthErrorCode.INVALID_TOKEN);
        }

        // 다음 필터 진행
        filterChain.doFilter(request, response);
    }
}
