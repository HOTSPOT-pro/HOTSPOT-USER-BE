package hotspot.user.auth.domain.mapper;

import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.auth.domain.Token;

/**
 * Token 도메인 -> 응답 Dto로 매핑
 */
public class TokenResponseMapper {
    public static TokenResponse toTokenResponse(Token token) {
        return new TokenResponse(token.getAccessToken(), token.getRefreshToken());
    }
}
