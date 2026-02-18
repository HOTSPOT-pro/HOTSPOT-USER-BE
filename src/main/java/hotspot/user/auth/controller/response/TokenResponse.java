package hotspot.user.auth.controller.response;


import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 토큰 응답 DTO
 */
public record TokenResponse(
        String accessToken,
        @JsonIgnore // refreshToken은 쿠키에만 담기도록
        String refreshToken
) {
}
