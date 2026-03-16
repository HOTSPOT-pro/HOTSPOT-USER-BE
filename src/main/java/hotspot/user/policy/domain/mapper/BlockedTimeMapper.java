package hotspot.user.policy.domain.mapper;

import java.util.List;
import java.util.stream.Collectors;

import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.controller.response.BlockedRangeResponse;
import hotspot.user.policy.controller.response.BlockedTimeResponse;
import hotspot.user.policy.controller.response.DayBlockedTimeResponse;
import hotspot.user.policy.domain.BlockedTime;

/**
 * 차단 시간대 정보를 DTO로 변환하는 매퍼
 */
public class BlockedTimeMapper {

    /**
     * BlockedTime 도메인 객체를 BlockedTimeResponse DTO로 변환
     */
    public static BlockedTimeResponse toBlockedTimeResponse(
            AppliedPolicyResponse policyResponse,
            BlockedTime blockedTime
    ) {
        List<DayBlockedTimeResponse> dayBlockedTimes = blockedTime.getDayRanges().entrySet().stream()
                .map(entry -> DayBlockedTimeResponse.builder()
                        .day(entry.getKey())
                        .ranges(entry.getValue().stream()
                                .map(range -> BlockedRangeResponse.builder()
                                        .startTime(range.start())
                                        .endTime(range.end())
                                        .build())
                                .toList())
                        .build())
                .sorted((o1, o2) -> o1.day().compareTo(o2.day()))
                .collect(Collectors.toList());

        return BlockedTimeResponse.builder()
                .subId(policyResponse.subId())
                .dayBlockedTimes(dayBlockedTimes)
                .build();
    }
}
