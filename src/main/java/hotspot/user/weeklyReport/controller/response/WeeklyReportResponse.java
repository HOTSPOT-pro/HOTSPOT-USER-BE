package hotspot.user.weeklyReport.controller.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;

@Builder
public record WeeklyReportResponse(
        Long subId,
        String name,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        Overview overview,
        DailyUsage dailyUsage,
        HourlyUsage hourlyUsage,
        CategoryUsageList categoryUsageList,
        FinalFeedback final_feedback
) {
    @Builder
    public record Overview(
            ScoreDataResponse scoreData,
            List<String> tags
    ) {}

    @Builder
    public record ScoreDataResponse(
            Integer totalScore,
            String scoreLevel,
            Integer scoreDiff,
            List<ScoreReasonResponse> reason
    ) {}

    @Builder
    public record ScoreReasonResponse(
            Integer value,
            String exp
    ) {}

    @Builder
    public record DailyUsage(
            Long weekdayAvg,
            Long weekdayAvgDiff,
            Double weekdayAvgChangeRate,
            Long weekendAvg,
            Long weekendAvgDiff,
            Double weekendAvgChangeRate,
            String ai_feedback,
            List<DailyUsageItem> dailyUsageList
    ) {}

    @Builder
    public record DailyUsageItem(
            DayOfWeek day,
            Long lastWeek,
            Long thisWeek
    ) {}

    @Builder
    public record HourlyUsage(
            Long lateNightUsage,
            Long lateNightUsageDiff,
            Double lateNightUsageChangeRate,
            Long studyTimeUsage,
            Long studyTimeUsageDiff,
            Double studyTimeUsageChangeRate,
            String ai_feedback,
            List<HourlyUsageItem> hourlyUsageList
    ) {}

    @Builder
    public record HourlyUsageItem(
            Integer hour,
            Boolean isLateNight,
            Boolean isStudyTime,
            Long lastWeek,
            Long thisWeek
    ) {}

    @Builder
    public record CategoryUsageList(
            String ai_feedback,
            List<CategoryUsageItem> thisWeek,
            List<CategoryUsageItem> lastWeek,
            List<CategoryComparison> comparison
    ) {}

    @Builder
    public record CategoryUsageItem(
            String category,
            Long usage,
            Double percent
    ) {}

    @Builder
    public record CategoryComparison(
            String category,
            Double changeRate
    ) {}

    @Builder
    public record FinalFeedback(
            String parent,
            String child,
            List<PolicyRecommend> policyRecommendList
    ) {}

    @Builder
    public record PolicyRecommend(
            String title,
            String description,
            String reason
    ) {}
}
