package hotspot.user.auth.domain;

import hotspot.user.auth.controller.request.TokenRequest;
import lombok.Builder;
import lombok.Getter;

// 토큰 (회원 id, AccessToken, RefreshToken)
@Getter
public class Token {
    private Long memberId;

    private String accessToken;

    private String refreshToken;

    @Builder
    public Token(Long memberId, String accessToken, String refreshToken) {
        this.memberId = memberId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static Token create(Long memberId, TokenRequest request) {
        return Token.builder()
                .memberId(memberId)
                .refreshToken(request.refreshToken())
                .build();
    }
}
