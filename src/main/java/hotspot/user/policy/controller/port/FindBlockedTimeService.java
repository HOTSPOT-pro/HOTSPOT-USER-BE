package hotspot.user.policy.controller.port;

import java.util.List;

import hotspot.user.policy.controller.response.BlockedTimeResponse;

public interface FindBlockedTimeService {
    /**
     * 본인(회원)의 요일별 차단 시간대 조회
     */
    BlockedTimeResponse findMemberBlockedTime(Long memberId);

    /**
     * 가족 전체 구성원의 요일별 차단 시간대 조회
     */
    List<BlockedTimeResponse> findFamilyBlockedTime(Long memberId);
}
