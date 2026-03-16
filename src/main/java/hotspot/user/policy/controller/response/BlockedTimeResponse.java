package hotspot.user.policy.controller.response;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import lombok.Builder;

@Builder
public record BlockedTimeResponse(
        Long subId,                      // 회선 식별자
        List<DayBlockedTimeResponse> dayBlockedTimes
) {
    @Builder
    public record DayBlockedTimeResponse(
            DayOfWeek day,               // 요일
            List<BlockedRangeResponse> ranges // 차단 시간대
    ) {}

    @Builder
    public record BlockedRangeResponse(
            LocalTime startTime,         // 차단 시작 (예: 22:00)
            LocalTime endTime           // 차단 종료 (예: 06:00)
    ) {}
}
