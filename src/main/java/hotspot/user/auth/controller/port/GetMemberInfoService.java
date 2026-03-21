package hotspot.user.auth.controller.port;

import hotspot.user.auth.controller.response.MemberInfoResponse;

/**
 * 로그인 이후 멤버 정보 조회 서비스
 */
public interface GetMemberInfoService {
    MemberInfoResponse getMemberInfo(Long memberId, String email);
}
