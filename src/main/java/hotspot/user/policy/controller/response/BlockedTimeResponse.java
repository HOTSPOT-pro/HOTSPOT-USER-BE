package hotspot.user.policy.controller.response;

import java.util.List;

import lombok.Builder;

@Builder
public record BlockedTimeResponse(
        Long subId,                      // 회선 식별자
        List<DayBlockedTimeResponse> dayBlockedTimes
) {}
