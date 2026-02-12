package hotspot.user.member.controller.port;

import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.Member;

/**
 * 소셜 로그인 시 사용자 정보를 로드하거나 신규 회원을 생성하는 서비스 인터페이스
 */
public interface LoadSocialUserService {
    Member loadOrCreateMember(String name, CreateSocialAccountRequest request);
}
