package hotspot.user.auth.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.domain.Token;
import hotspot.user.auth.service.port.TokenRepository;

/**
 * 토큰 저장 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class SaveTokenServiceImplTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private SaveTokenServiceImpl saveTokenService;

    @Test
    @DisplayName("토큰 저장 성공: memberId와 함께 리프레시 토큰을 저장한다")
    void saveTokenSuccess() {
        // given
        Long memberId = 1L;
        TokenRequest request = new TokenRequest("refresh-token");

        // when
        saveTokenService.saveToken(memberId, request);

        // then
        verify(tokenRepository).save(any(Token.class));
    }
}
