package hotspot.user.auth.controller.request;


/**
 * 토큰 요청 DTO
 * memberId는 Claims에서 추출해서 사용하도록
 */
public record TokenRequest(
        String refreshToken
) { }
