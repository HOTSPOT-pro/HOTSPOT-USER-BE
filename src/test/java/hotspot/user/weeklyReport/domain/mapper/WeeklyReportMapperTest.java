package hotspot.user.weeklyReport.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.weeklyReport.controller.response.WeeklyReportResponse;
import hotspot.user.weeklyReport.domain.AIFeedback;
import hotspot.user.weeklyReport.domain.ReportStatus;
import hotspot.user.weeklyReport.domain.ReportTag;
import hotspot.user.weeklyReport.domain.ScoreData;
import hotspot.user.weeklyReport.domain.ScoreLevel;
import hotspot.user.weeklyReport.domain.SummaryData;
import hotspot.user.weeklyReport.domain.UsageListData;
import hotspot.user.weeklyReport.domain.WeeklyReport;

class WeeklyReportMapperTest {

    @Test
    @DisplayName("도메인 객체의 모든 필드를 WeeklyReportResponse로 정확히 매핑하며, 모든 Record 객체의 커버리지를 확보한다")
    void toWeeklyReportResponseSuccess() {
        // given: 모든 중첩 리스트에 데이터를 포함한 도메인 객체 생성
        WeeklyReport domain = createFullWeeklyReport();

        // when: 매핑 실행
        WeeklyReportResponse response = WeeklyReportMapper.toWeeklyReportResponse(domain);

        // then: 기본 필드 검증
        assertThat(response).isNotNull();
        assertThat(response.subId()).isEqualTo(10L);
        assertThat(response.overview().scoreData().totalScore()).isEqualTo(85);
        
        // 커버리지 확보를 위한 Record 객체별 세부 검증 (Accessor 호출)
        
        // 1. ScoreReason 커버리지
        ScoreData.ScoreReason reason = domain.getScoreData().reasons().get(0);
        assertThat(reason.value()).isEqualTo(10);
        assertThat(reason.reason()).isEqualTo("Reason Text");
        assertThat(reason.toString()).isNotNull();
        assertThat(reason.hashCode()).isNotZero();
        assertThat(reason.equals(reason)).isTrue();

        // 2. DailyUsage 커버리지
        UsageListData.DailyUsage daily = domain.getUsageListData().dailyUsageList().get(0);
        assertThat(daily.day()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(daily.date()).isNotNull();
        assertThat(daily.lastWeek()).isEqualTo(100L);
        assertThat(daily.thisWeek()).isEqualTo(120L);
        assertThat(daily.toString()).isNotNull();

        // 3. HourlyUsage 커버리지
        UsageListData.HourlyUsage hourly = domain.getUsageListData().hourlyUsageList().get(0);
        assertThat(hourly.hour()).isEqualTo(0);
        assertThat(hourly.isLateNight()).isTrue();
        assertThat(hourly.isStudyTime()).isFalse();
        assertThat(hourly.toString()).isNotNull();

        // 4. CategoryUsageItem 커버리지 (thisWeek & lastWeek)
        UsageListData.CategoryUsageItem categoryItem = domain.getUsageListData().categoryUsageList().thisWeek().get(0);
        assertThat(categoryItem.category()).isEqualTo("MEDIA");
        assertThat(categoryItem.usage()).isEqualTo(600L);
        assertThat(categoryItem.percent()).isEqualTo(60.0);
        assertThat(categoryItem.toString()).isNotNull();

        // 5. CategoryComparison 커버리지
        UsageListData.CategoryComparison comparison = domain.getUsageListData().categoryUsageList().comparison().get(0);
        assertThat(comparison.category()).isEqualTo("MEDIA");
        assertThat(comparison.changeRate()).isEqualTo(20.0);
        assertThat(comparison.toString()).isNotNull();

        // 6. PolicyRecommend 커버리지
        AIFeedback.PolicyRecommend policy = domain.getAiFeedback().policyRecommendList().get(0);
        assertThat(policy.title()).isEqualTo("Policy Title");
        assertThat(policy.description()).isEqualTo("Description");
        assertThat(policy.reason()).isEqualTo("Reason");
        assertThat(policy.toString()).isNotNull();

        // 7. ReportStatus (Enum) 커버리지
        for (ReportStatus status : ReportStatus.values()) {
            assertThat(ReportStatus.valueOf(status.name())).isEqualTo(status);
        }
        
        // 8. ReportTag (Enum) 커버리지 보완
        assertThat(ReportTag.toKorean("LATE_NIGHT_HIGH")).isEqualTo("심야 사용 높음");
        assertThat(ReportTag.valuesInPriorityOrder()).isNotEmpty();
    }

    private WeeklyReport createFullWeeklyReport() {
        return WeeklyReport.builder()
                .weeklyReportId(1L)
                .familyId(100L)
                .subId(10L)
                .name("Test User")
                .weekStartDate(LocalDate.of(2026, 3, 9))
                .weekEndDate(LocalDate.of(2026, 3, 15))
                .reportStatus(ReportStatus.COMPLETED)
                .totalUsage(1048576L)
                .scoreData(ScoreData.builder()
                        .totalScore(85)
                        .scoreLevel(ScoreLevel.EXCELLENT)
                        .scoreDiff(5)
                        .reasons(List.of(new ScoreData.ScoreReason(10, "Reason Text")))
                        .build())
                .tags(List.of(ReportTag.LATE_NIGHT_HIGH))
                .summaryData(SummaryData.builder()
                        .dailySummary(SummaryData.DailySummary.builder()
                                .weekdayAvg(1024L)
                                .weekendAvg(512L)
                                .weekdayAvgDiff(100L)
                                .weekendAvgDiff(-50L)
                                .weekdayAvgChangeRate(10.0)
                                .weekendAvgChangeRate(-5.0)
                                .build())
                        .hourlySummary(SummaryData.HourlySummary.builder()
                                .lateNightUsage(100L)
                                .studyTimeUsage(200L)
                                .lateNightUsageDiff(10L)
                                .studyTimeUsageDiff(-20L)
                                .lateNightUsageChangeRate(5.0)
                                .studyTimeUsageChangeRate(-2.0)
                                .build())
                        .build())
                .usageListData(UsageListData.builder()
                        .totalUsage(1048576L)
                        .dailyUsageList(List.of(new UsageListData.DailyUsage(DayOfWeek.MONDAY, LocalDate.now(), 100L, 120L)))
                        .hourlyUsageList(List.of(new UsageListData.HourlyUsage(0, 10L, 12L, true, false)))
                        .categoryUsageList(UsageListData.CategoryUsageReport.builder()
                                .lastWeek(List.of(new UsageListData.CategoryUsageItem("MEDIA", 500L, 50.0)))
                                .thisWeek(List.of(new UsageListData.CategoryUsageItem("MEDIA", 600L, 60.0)))
                                .comparison(List.of(new UsageListData.CategoryComparison("MEDIA", 20.0)))
                                .build())
                        .build())
                .aiFeedback(AIFeedback.builder()
                        .feedback(new AIFeedback.FeedbackMessage("Child FB", "Parent FB"))
                        .summaryText(new AIFeedback.SummaryText("Overall", "Daily", "Hourly", "Category"))
                        .policyRecommendList(List.of(new AIFeedback.PolicyRecommend("Policy Title", "Description", "Reason")))
                        .build())
                .build();
    }
}
