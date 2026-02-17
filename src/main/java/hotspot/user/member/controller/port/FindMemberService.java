package hotspot.user.member.controller.port;

import hotspot.user.member.controller.response.MemberResponse;


/**
 * 회원 조회 서비스 인터페이스
 */
public interface FindMemberService {
    MemberResponse findById(Long id);
}
