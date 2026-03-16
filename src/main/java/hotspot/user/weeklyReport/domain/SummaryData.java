package hotspot.user.weeklyReport.domain;

import lombok.Builder;

@Builder
public record SummaryData(
        DailySummary dailySummary,
        HourlySummary hourlySummary
) {
    @Builder
    public record DailySummary(
            Long weekdayAvg,
            Long weekendAvg,
            Long weekdayAvgDiff,
            Long weekendAvgDiff,
            Double weekdayAvgChangeRate,
            Double weekendAvgChangeRate
    ) {}

    @Builder
    public record HourlySummary(
            Long lateNightUsage,
            Long studyTimeUsage,
            Long lateNightUsageDiff,
            Long studyTimeUsageDiff,
            Double lateNightUsageChangeRate,
            Double studyTimeUsageChangeRate
    ) {}
}
