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
        List<CategoryUsage> categoryUsageList
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
    public record CategoryUsage(
            String category,
            Long lastWeek,
            Long thisWeek,
            Double changeRate
    ) {}
}
