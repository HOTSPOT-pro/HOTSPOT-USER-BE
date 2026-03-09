package hotspot.user.policy.controller.port;

import hotspot.user.policy.controller.response.BlockedStatusResponse;

/**
 * 구성원의 데이터 사용량 차단 여부 조회하는 서비스
 */
public interface FindBlockStatusService {
    /**
     * 내 상세 차단 상태 조회 (즉시 차단, 정책 차단, 사유 포함)
     */
    BlockedStatusResponse findMyBlockStatus(Long memberId);

    /**
     * 특정 회선의 현재 차단 여부만 확인 (가족 구성원 리스트용)
     */
    boolean isCurrentlyBlocked(Long subId);
}
