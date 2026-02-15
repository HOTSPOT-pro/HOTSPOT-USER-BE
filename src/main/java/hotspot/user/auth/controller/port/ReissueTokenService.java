package hotspot.user.auth.controller.port;

import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.controller.response.TokenResponse;

/**
 * 토큰 재발급 RTR
 */
public interface ReissueTokenService {
    TokenResponse reissue(TokenRequest request);
}
