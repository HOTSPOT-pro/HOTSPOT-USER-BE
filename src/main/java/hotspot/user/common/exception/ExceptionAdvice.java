package hotspot.user.common.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.BaseErrorCode;
import hotspot.user.common.exception.code.GlobalErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

  // AccessDeniedException - 인가 예외 (에: 권한 부족)
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
    AuthErrorCode code = AuthErrorCode.ACCESS_DENIED;
    log.error("[AccessDeniedException] {} - {}", code.getCustomCode(), code.getMessage());

    ErrorResponse response =
            new ErrorResponse(code.getHttpStatus().value(), code.getCustomCode(), code.getMessage());

    return ResponseEntity.status(code.getHttpStatus()).body(response);
  }

  // AuthenticationException - 시큐리티 인증 예외 (예: 토큰 만료, 유효하지 않은 토큰)
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Object> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
    // JwtProvider/Filter에서 request에 담아둔 에러 코드를 확인한다.
    Object attribute = request.getAttribute("exception");
    AuthErrorCode code = AuthErrorCode.INVALID_TOKEN; // 기본값

    if (attribute instanceof AuthErrorCode) {
      code = (AuthErrorCode) attribute;
    }

    log.error("[AuthenticationException] {} - {}", code.getCustomCode(), code.getMessage());

    ErrorResponse response =
            new ErrorResponse(code.getHttpStatus().value(), code.getCustomCode(), code.getMessage());

    return ResponseEntity.status(code.getHttpStatus()).body(response);
  }

  /** BaseException - 도메인 예외 (ex: ApplicationException) */
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<Object> handleBaseException(BaseException e, HttpServletRequest request) {
    BaseErrorCode code = e.getCode();
    log.error("[BaseException] {} - {}", code.name(), code.getMessage(), e);

    ErrorResponse response =
        new ErrorResponse(code.getHttpStatus().value(), code.getCustomCode(), code.getMessage());

    return ResponseEntity.status(code.getHttpStatus()).body(response);
  }

  /** 그 외 모든 예외 */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleUnhandledException(Exception e, WebRequest request) {
    log.error("[Exception] Unhandled: {}", e.getMessage(), e);

    GlobalErrorCode code = GlobalErrorCode.INTERNAL_SERVER_ERROR;

    ErrorResponse response =
        new ErrorResponse(code.getHttpStatus().value(), code.getCustomCode(), code.getMessage());

    return ResponseEntity.status(code.getHttpStatus()).body(response);
  }
}
