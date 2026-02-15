package hotspot.user.member.controller.port;

import java.util.Optional;

import hotspot.user.member.domain.Member;

/**
 * 회원 조회 서비스 인터페이스
 */
public interface FindMemberService {
    Optional<Member> findByEmail(String email);
}
