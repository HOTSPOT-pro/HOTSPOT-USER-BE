package hotspot.user.auth.service.port;

import java.util.Optional;

import hotspot.user.auth.domain.Token;

/**
 * 토큰 저장소 인터페이스 (Out-Port)
 */
public interface TokenRepository {
    void save(Token token);

    Optional<Token> findByMemberId(Long memberId);

    Optional<Token> findByRefreshToken(String refreshToken);

    void deleteByMemberId(Long memberId);
}
