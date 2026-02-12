package hotspot.user.auth.controller.response;


/**
 * 토큰 응답 DTO
 */
public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
