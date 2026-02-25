package hotspot.user.common.security.jwt;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import lombok.extern.slf4j.Slf4j;

/**
 * 토큰은 정상인데 권한이 없는 경우(403) 처리
 * 필터 체인에서 발생한 인가 예외를 ExceptionAdvice로 위임
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final HandlerExceptionResolver resolver;

    public JwtAccessDeniedHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.error("인가 실패(권한 없음) - URI: {}, 사유: {}", request.getRequestURI(), accessDeniedException.getMessage());

        // ExceptionAdvice(@RestControllerAdvice)에서 처리할 수 있도록 위임
        resolver.resolveException(request, response, null, accessDeniedException);
    }
}