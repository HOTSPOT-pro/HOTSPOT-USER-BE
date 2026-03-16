package hotspot.user.weeklyReport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.WeeklyReportErrorCode;
import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.weeklyReport.controller.response.WeeklyReportResponse;
import hotspot.user.weeklyReport.domain.AIFeedback;
import hotspot.user.weeklyReport.domain.ReportTag;
import hotspot.user.weeklyReport.domain.ScoreData;
import hotspot.user.weeklyReport.domain.ScoreLevel;
import hotspot.user.weeklyReport.domain.SummaryData;
import hotspot.user.weeklyReport.domain.UsageListData;
import hotspot.user.weeklyReport.domain.WeeklyReport;
import hotspot.user.weeklyReport.service.port.WeeklyReportRepository;

@ExtendWith(MockitoExtension.class)
class FindWeeklyReportServiceImplTest {

    @Mock
    private WeeklyReportRepository weeklyReportRepository;

    @Mock
    private FindFamilySubscriptionService familySubscriptionService;

    @InjectMocks
    private FindWeeklyReportServiceImpl service;

    @Test
    @DisplayName("정상적인 권한과 소유권을 가진 경우 리포트를 성공적으로 조회한다")
    void findWeeklyReportSuccess() {
        // given
        Long requesterMemberId = 1L;
        Long subId = 10L;
        Long reportId = 100L;
        Long familyId = 1000L;

        Family family = Family.builder().id(familyId).build();
        FamilySubscription requesterSub = FamilySubscription.builder().family(family).build();
        FamilySubscription targetSub = FamilySubscription.builder().family(family).build();

        given(familySubscriptionService.findByMemberId(requesterMemberId)).willReturn(requesterSub);
        given(familySubscriptionService.findBySubId(subId)).willReturn(targetSub);

        WeeklyReport report = createWeeklyReport(reportId, subId);
        given(weeklyReportRepository.findByReportId(reportId)).willReturn(Optional.of(report));

        // when
        WeeklyReportResponse response = service.findWeeklyReport(requesterMemberId, subId, reportId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.subId()).isEqualTo(subId);
        assertThat(response.overview().tags()).contains("심야 사용 높음");
    }

    @Test
    @DisplayName("요청자와 대상이 다른 가족인 경우 예외가 발생한다")
    void findWeeklyReportFailDifferentFamily() {
        // given
        Long requesterMemberId = 1L;
        Long subId = 10L;
        Long reportId = 100L;

        FamilySubscription requesterSub = FamilySubscription.builder().family(Family.builder().id(1L).build()).build();
        FamilySubscription targetSub = FamilySubscription.builder().family(Family.builder().id(2L).build()).build();

        given(familySubscriptionService.findByMemberId(requesterMemberId)).willReturn(requesterSub);
        given(familySubscriptionService.findBySubId(subId)).willReturn(targetSub);

        // when & then
        assertThatThrownBy(() -> service.findWeeklyReport(requesterMemberId, subId, reportId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.NOT_FAMILY_MEMBER.getMessage());
    }

    @Test
    @DisplayName("리포트가 요청된 subId의 소유가 아닌 경우 예외가 발생한다")
    void findWeeklyReportFailOwnership() {
        // given
        Long requesterMemberId = 1L;
        Long subId = 10L;
        Long reportId = 100L;
        Long familyId = 1000L;
        Long differentSubId = 11L;

        Family family = Family.builder().id(familyId).build();
        FamilySubscription requesterSub = FamilySubscription.builder().family(family).build();
        FamilySubscription targetSub = FamilySubscription.builder().family(family).build();

        given(familySubscriptionService.findByMemberId(requesterMemberId)).willReturn(requesterSub);
        given(familySubscriptionService.findBySubId(subId)).willReturn(targetSub);

        WeeklyReport report = createWeeklyReport(reportId, differentSubId);
        given(weeklyReportRepository.findByReportId(reportId)).willReturn(Optional.of(report));

        // when & then
        assertThatThrownBy(() -> service.findWeeklyReport(requesterMemberId, subId, reportId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(WeeklyReportErrorCode.REPORT_ACCESS_DENIED.getMessage());
    }

    private WeeklyReport createWeeklyReport(Long reportId, Long subId) {
        return WeeklyReport.builder()
                .weeklyReportId(reportId)
                .subId(subId)
                .name("Test Report")
                .weekStartDate(LocalDate.now())
                .weekEndDate(LocalDate.now())
                .scoreData(ScoreData.builder()
                        .totalScore(80)
                        .scoreLevel(ScoreLevel.EXCELLENT)
                        .scoreDiff(5)
                        .reasons(java.util.List.of())
                        .build())
                .tags(java.util.List.of(ReportTag.LATE_NIGHT_HIGH))
                .summaryData(SummaryData.builder()
                        .dailySummary(SummaryData.DailySummary.builder()
                                .weekdayAvg(1024L * 1024L)
                                .weekendAvg(0L)
                                .weekdayAvgDiff(0L)
                                .weekendAvgDiff(0L)
                                .weekdayAvgChangeRate(0.0)
                                .weekendAvgChangeRate(0.0)
                                .build())
                        .hourlySummary(SummaryData.HourlySummary.builder()
                                .lateNightUsage(512L * 1024L)
                                .studyTimeUsage(0L)
                                .lateNightUsageDiff(0L)
                                .studyTimeUsageDiff(0L)
                                .lateNightUsageChangeRate(0.0)
                                .studyTimeUsageChangeRate(0.0)
                                .build())
                        .build())
                .usageListData(UsageListData.builder()
                        .dailyUsageList(java.util.List.of())
                        .hourlyUsageList(java.util.List.of())
                        .categoryUsageList(UsageListData.CategoryUsageReport.builder()
                                .thisWeek(java.util.List.of())
                                .lastWeek(java.util.List.of())
                                .comparison(java.util.List.of())
                                .build())
                        .build())
                .aiFeedback(AIFeedback.builder()
                        .summaryText(AIFeedback.SummaryText.builder()
                                .overall("Overall")
                                .daily("Daily")
                                .hourly("Hourly")
                                .category("Category")
                                .build())
                        .feedback(AIFeedback.FeedbackMessage.builder()
                                .toChild("Child")
                                .toParent("Parent")
                                .build())
                        .policyRecommendList(java.util.List.of())
                        .build())
                .build();
    }
}
