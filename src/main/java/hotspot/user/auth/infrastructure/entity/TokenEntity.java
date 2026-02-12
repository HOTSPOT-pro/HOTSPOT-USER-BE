package hotspot.user.auth.infrastructure.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import hotspot.user.auth.domain.Token;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 엔티티 (Redis 저장용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 7 * 2)
public class TokenEntity {

    @Id
    private Long memberId;

    @Indexed
    private String refreshToken;

    public static TokenEntity domainToEntity(Token token) {
        return TokenEntity.builder()
                .memberId(token.getMemberId())
                .refreshToken(token.getRefreshToken())
                .build();
    }

    public Token entityToDomain() {
        return Token.builder()
                .memberId(memberId)
                .refreshToken(refreshToken)
                .build();
    }
}
