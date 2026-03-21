package hotspot.user.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.auth.domain.Token;
import hotspot.user.auth.infrastructure.entity.TokenEntity;
import hotspot.user.common.security.jwt.JwtProperties;

/**
 * 토큰 Repository 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class TokenRepositoryImplTest {

    @Mock
    private TokenCrudRepository tokenCrudRepository;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private TokenRepositoryImpl tokenRepository;

    @Test
    @DisplayName("토큰 저장 성공: Redis 엔티티로 변환하여 저장하고 다시 도메인으로 복구한다")
    void saveTokenSuccess() {
        // given
        Token token = Token.builder()
                .memberId(1L)
                .refreshToken("rt")
                .accessToken("at")
                .build();

        TokenEntity entity = new TokenEntity(1L, "rt", 3600L);

        given(jwtProperties.getRefreshExpiration()).willReturn(3600000L);
        given(tokenCrudRepository.save(any(TokenEntity.class))).willReturn(entity);

        // when
        Token result = tokenRepository.save(token);

        // then
        assertThat(result.getRefreshToken()).isEqualTo("rt");
        assertThat(result.getMemberId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("멤버 ID로 토큰 조회 성공")
    void findByMemberIdSuccess() {
        // given
        Long memberId = 1L;
        TokenEntity entity = new TokenEntity(memberId, "rt", 3600L);
        given(tokenCrudRepository.findById(memberId)).willReturn(Optional.of(entity));

        // when
        Optional<Token> result = tokenRepository.findByMemberId(memberId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getRefreshToken()).isEqualTo("rt");
    }

    @Test
    @DisplayName("리프레시 토큰으로 토큰 조회 성공")
    void findByRefreshTokenSuccess() {
        // given
        String refreshToken = "rt";
        TokenEntity entity = new TokenEntity(1L, refreshToken, 3600L);
        given(tokenCrudRepository.findByRefreshToken(refreshToken)).willReturn(Optional.of(entity));

        // when
        Optional<Token> result = tokenRepository.findByRefreshToken(refreshToken);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getMemberId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("멤버 ID로 토큰 삭제 성공")
    void deleteByMemberIdSuccess() {
        // given
        Long memberId = 1L;

        // when
        tokenRepository.deleteByMemberId(memberId);

        // then
        verify(tokenCrudRepository).deleteById(memberId);
    }
}
