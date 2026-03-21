package hotspot.user.auth.controller.port;

import hotspot.user.auth.controller.request.TokenRequest;

/**
 * 회원 탈퇴 서비스 포트
 */
public interface WithdrawService {

    /**
     * 회원 탈퇴를 처리
     * @param memberId
     * @param request
     */
    void withdraw(Long memberId, TokenRequest request);
}
