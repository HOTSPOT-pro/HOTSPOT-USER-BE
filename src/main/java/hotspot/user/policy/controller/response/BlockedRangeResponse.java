package hotspot.user.policy.controller.response;

import java.time.LocalTime;

import lombok.Builder;

@Builder
public record BlockedRangeResponse(
        LocalTime startTime,         // 차단 시작 (예: 22:00)
        LocalTime endTime            // 차단 종료 (예: 06:00)
) {}
