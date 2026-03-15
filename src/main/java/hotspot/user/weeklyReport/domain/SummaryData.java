package hotspot.user.weeklyReport.domain;

import java.util.List;

import lombok.Builder;

@Builder
public record SummaryData(
        DailySummary dailySummary,
        HourlySummary hourlySummary,
        List<CategorySummary> categorySummary
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

    @Builder
    public record CategorySummary(
            String category,
            Double percent
    ) {}
}
