package hotspot.user.common.security.jwt;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import lombok.extern.slf4j.Slf4j;

/**
 * 인증 실패(401) 시 호출되는 엔드포인트
 * 필터 체인에서 발생한 예외를 ExceptionAdvice로 위임
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.error("인증 예외 발생: {}, ExceptionAdvice로 위임합니다.", authException.getMessage());

        // ExceptionAdvice(@RestControllerAdvice)에서 처리할 수 있도록 위임
        resolver.resolveException(request, response, null, authException);
    }
}
