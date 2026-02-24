package hotspot.user.auth.controller.port;

import hotspot.user.auth.controller.request.TokenRequest;

/**
 * 회원 탈퇴 서비스 포트
 */
public interface WithdrawService {
    /**
     * 회원 탈퇴를 처리합니다. (토큰 삭제, 회선 연결 해제, 소셜 계정 삭제, 회원 삭제)
     * @param memberId 탈퇴할 회원의 ID
     */
    void withdraw(Long memberId);
}
