package hotspot.user.weeklyReport.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;

@Builder
public record UsageListData(
        Long totalUsage,
        List<DailyUsage> dailyUsageList,
        List<HourlyUsage> hourlyUsageList,
        CategoryUsageReport categoryUsageList
) {
    @Builder
    public record DailyUsage(
            DayOfWeek day,
            LocalDate date,
            Long lastWeek,
            Long thisWeek
    ) {}

    @Builder
    public record HourlyUsage(
            Integer hour,
            Long lastWeek,
            Long thisWeek,
            Boolean isLateNight,
            Boolean isStudyTime
    ) {}

    @Builder
    public record CategoryUsageReport(
            List<CategoryUsageItem> lastWeek,
            List<CategoryUsageItem> thisWeek,
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
}
