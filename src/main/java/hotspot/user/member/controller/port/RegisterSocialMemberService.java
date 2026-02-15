package hotspot.user.member.controller.port;

import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.Member;

/**
 * 소셜 회원 가입 서비스 인터페이스
 */
public interface RegisterSocialMemberService {
    Member register(CreateSocialAccountRequest request);
}
