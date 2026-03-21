package hotspot.user.member.controller.port;

import hotspot.user.auth.controller.response.LoginResponse;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;

/**
 * 소셜 로그인 서비스 인터페이스 (Facade)
 */
public interface SocialLoginService {
    LoginResponse login(CreateSocialAccountRequest request);
}
