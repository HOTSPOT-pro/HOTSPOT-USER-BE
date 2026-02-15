package hotspot.user.auth.controller.port;

import hotspot.user.auth.controller.request.TokenRequest;

public interface SaveTokenService {
    void saveToken(Long memberId, TokenRequest request);
}
