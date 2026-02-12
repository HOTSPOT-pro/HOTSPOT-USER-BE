package hotspot.user.auth.infrastructure;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import hotspot.user.auth.infrastructure.entity.TokenEntity;

/**
 * 토큰 저장소 (Spring Data Redis)
 */
@Repository
public interface TokenCrudRepository extends CrudRepository<TokenEntity, Long> {
    Optional<TokenEntity> findByRefreshToken(String refreshToken);
}
