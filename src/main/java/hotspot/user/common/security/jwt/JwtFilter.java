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
        String token = jwtProvider.resolveToken(request);
        String requestURI = request.getRequestURI();

        // 토큰 유효성 검사
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            // 3. 유효한 토큰일 경우 인증 객체 생성 및 Context 저장
            Authentication authentication = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}",

                                authentication.getName(), requestURI);
        } else {
            log.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }

        // 4. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}
