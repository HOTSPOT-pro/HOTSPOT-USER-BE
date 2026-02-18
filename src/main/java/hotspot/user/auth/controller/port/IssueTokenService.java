package hotspot.user.auth.controller.port;

import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;

/**
 * 토큰 발급 및 로그인을 처리하는 서비스
 */
public interface IssueTokenService {
    TokenResponse issue(Member member, String email, FamilyRole familyRole);
}
