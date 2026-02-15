package hotspot.user.auth.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.auth.domain.Token;
import hotspot.user.auth.infrastructure.entity.TokenEntity;
import hotspot.user.auth.service.port.TokenRepository;
import hotspot.user.common.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;

/**
 * TokenRepository 구현체 (Adapter)
 */
@Repository
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {

    private final TokenCrudRepository tokenCrudRepository;
    private final JwtProperties jwtProperties;

    @Override
    public void save(Token token) {
        // ms -> 초 변환
        tokenCrudRepository.save(TokenEntity.domainToEntity(token, jwtProperties.getRefreshExpiration() / 1000));
    }

    @Override
    public Optional<Token> findByMemberId(Long memberId) {
        return tokenCrudRepository
                .findById(memberId)
                .map(TokenEntity::entityToDomain);
    }

    @Override
    public Optional<Token> findByRefreshToken(String refreshToken) {
        return tokenCrudRepository
                .findByRefreshToken(refreshToken)
                .map(TokenEntity::entityToDomain);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        tokenCrudRepository.deleteById(memberId);
    }
}
