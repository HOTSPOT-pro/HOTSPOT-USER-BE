package hotspot.user.weeklyReport.domain;

import java.util.List;

import lombok.Builder;

@Builder
public record ScoreData(
        Integer totalScore,
        ScoreLevel scoreLevel,
        Integer scoreDiff,
        List<ScoreReason> reasons

) {
    @Builder
    public record ScoreReason(
            Integer value,
            String reason
    ) {}
}
