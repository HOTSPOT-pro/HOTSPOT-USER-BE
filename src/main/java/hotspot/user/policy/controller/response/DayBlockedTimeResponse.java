package hotspot.user.policy.controller.response;

import java.time.DayOfWeek;
import java.util.List;

import lombok.Builder;

@Builder
public record DayBlockedTimeResponse(
        DayOfWeek day,               // 요일
        List<BlockedRangeResponse> ranges // 차단 시간대
) {}
