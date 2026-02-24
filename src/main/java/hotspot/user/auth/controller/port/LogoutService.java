package hotspot.user.auth.controller.port;

import hotspot.user.auth.controller.request.TokenRequest;

/**
 * 로그아웃 서비스 포트
 */
public interface LogoutService {
    /**
     * 로그아웃을 처리합니다. (토큰 삭제)
     * @param memberId 현재 로그인한 유저의 ID
     * @param request Refresh Token 정보를 담은 객체
     */
    void logout(Long memberId, TokenRequest request);
}
