package hotspot.user.member.controller.port;

import hotspot.user.auth.controller.response.LoginResponse;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;

/**
 * 소셜 회원 가입 서비스 인터페이스
 */
public interface RegisterSocialMemberService {
    LoginResponse register(CreateSocialAccountRequest request);
}
