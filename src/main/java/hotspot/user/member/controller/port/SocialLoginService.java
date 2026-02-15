package hotspot.user.member.controller.port;

import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.Member;

/**
 * 소셜 로그인 서비스 인터페이스 (Facade)
 */
public interface SocialLoginService {
    Member login(CreateSocialAccountRequest request);
}
