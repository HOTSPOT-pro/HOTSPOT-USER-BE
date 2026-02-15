package hotspot.user.auth.controller.port;

import hotspot.user.auth.controller.request.TokenRequest;

/**
 * 로그아웃
 */
public interface LogoutService {
    void logout(TokenRequest request);
}
