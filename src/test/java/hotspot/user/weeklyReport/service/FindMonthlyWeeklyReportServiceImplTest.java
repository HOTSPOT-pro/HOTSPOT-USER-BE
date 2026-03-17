package hotspot.user.weeklyReport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.member.domain.Member;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.weeklyReport.controller.response.MonthlyWeeklyReportResponse;
import hotspot.user.weeklyReport.domain.ReportStatus;
import hotspot.user.weeklyReport.domain.WeeklyReport;
import hotspot.user.weeklyReport.service.port.WeeklyReportRepository;

@ExtendWith(MockitoExtension.class)
class FindMonthlyWeeklyReportServiceImplTest {

    @Mock
    private WeeklyReportRepository weeklyReportRepository;

    @Mock
    private FindFamilySubscriptionService familySubscriptionService;

    @InjectMocks
    private FindMonthlyWeeklyReportServiceImpl service;

    @Test
    @DisplayName("같은 가족 구성원의 월별 주간 리포트 목록을 조회한다")
    void findMonthlyWeeklyReportsSuccess() {
        Long requesterMemberId = 1L;
        Long subId = 10L;
        YearMonth yearMonth = YearMonth.of(2026, 3);
        Family family = Family.builder().id(1000L).build();

        given(familySubscriptionService.findByMemberId(requesterMemberId))
                .willReturn(FamilySubscription.builder().family(family).build());
        given(familySubscriptionService.findBySubId(subId))
                .willReturn(FamilySubscription.builder()
                        .family(family)
                        .subscription(Subscription.builder()
                                .id(subId)
                                .member(Member.builder().name("홍길동").build())
                                .build())
                        .build());
        given(weeklyReportRepository.findMonthlyReportsBySubId(subId, yearMonth))
                .willReturn(List.of(
                        WeeklyReport.builder()
                                .weeklyReportId(101L)
                                .subId(subId)
                                .weekStartDate(LocalDate.of(2026, 3, 11))
                                .weekEndDate(LocalDate.of(2026, 3, 17))
                                .reportStatus(ReportStatus.COMPLETED)
                                .build(),
                        WeeklyReport.builder()
                                .weeklyReportId(100L)
                                .subId(subId)
                                .weekStartDate(LocalDate.of(2026, 3, 4))
                                .weekEndDate(LocalDate.of(2026, 3, 10))
                                .reportStatus(ReportStatus.COMPLETED)
                                .build()
                ));

        MonthlyWeeklyReportResponse response = service.findMonthlyWeeklyReports(
                requesterMemberId,
                subId,
                yearMonth
        );

        assertThat(response.subId()).isEqualTo(subId);
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.yearMonth()).isEqualTo(yearMonth);
        assertThat(response.reports()).hasSize(2);
        assertThat(response.reports().get(0).reportId()).isEqualTo(101L);
        assertThat(response.reports().get(0).title()).isEqualTo("2026년 3월 2주차 분석 리포트");
        assertThat(response.reports().get(0).period()).isEqualTo("2026.03.11~2026.03.17");
        assertThat(response.reports().get(1).title()).isEqualTo("2026년 3월 1주차 분석 리포트");
    }

    @Test
    @DisplayName("다른 가족 구성원의 월별 주간 리포트 목록은 조회할 수 없다")
    void findMonthlyWeeklyReportsFailDifferentFamily() {
        Long requesterMemberId = 1L;
        Long subId = 10L;

        given(familySubscriptionService.findByMemberId(requesterMemberId))
                .willReturn(FamilySubscription.builder()
                        .family(Family.builder().id(1L).build())
                        .build());
        given(familySubscriptionService.findBySubId(subId))
                .willReturn(FamilySubscription.builder()
                        .family(Family.builder().id(2L).build())
                        .build());

        assertThatThrownBy(() -> service.findMonthlyWeeklyReports(
                requesterMemberId,
                subId,
                YearMonth.of(2026, 3)
        ))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.NOT_FAMILY_MEMBER.getMessage());
    }
}
