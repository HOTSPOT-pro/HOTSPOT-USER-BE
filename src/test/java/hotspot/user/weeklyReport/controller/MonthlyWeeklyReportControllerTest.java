package hotspot.user.weeklyReport.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;
import hotspot.user.weeklyReport.controller.port.FindMonthlyWeeklyReportService;
import hotspot.user.weeklyReport.controller.port.FindWeeklyReportService;
import hotspot.user.weeklyReport.controller.response.MonthlyWeeklyReportResponse;
import hotspot.user.weeklyReport.domain.ReportStatus;

@WebMvcTest(WeeklyReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class MonthlyWeeklyReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FindWeeklyReportService findWeeklyReportService;

    @MockBean
    private FindMonthlyWeeklyReportService findMonthlyWeeklyReportService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private void setAuthentication(Long memberId) {
        PrincipalDetails principal = PrincipalDetails.builder()
                .id(memberId)
                .email("test@test.com")
                .familyId(100L)
                .role(FamilyRole.OWNER)
                .status(Status.APPROVED)
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("성공: 월별 주간 리포트 목록 조회 시 200 OK와 목록 데이터를 반환한다")
    void findMonthlyWeeklyReportsSuccess() throws Exception {
        Long memberId = 1L;
        Long subId = 10L;
        YearMonth yearMonth = YearMonth.of(2026, 3);
        setAuthentication(memberId);

        MonthlyWeeklyReportResponse response = MonthlyWeeklyReportResponse.builder()
                .subId(subId)
                .name("자녀 리포트")
                .yearMonth(yearMonth)
                .reports(java.util.List.of(
                        MonthlyWeeklyReportResponse.ReportItem.builder()
                                .reportId(101L)
                                .title("2026년 3월 2주차 분석 리포트")
                                .period("2026.03.11~2026.03.17")
                                .weekStartDate(LocalDate.of(2026, 3, 11))
                                .weekEndDate(LocalDate.of(2026, 3, 17))
                                .reportStatus(ReportStatus.COMPLETED)
                                .build()))
                .build();

        given(findMonthlyWeeklyReportService.findMonthlyWeeklyReports(memberId, subId, yearMonth))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/ai-reports/families/members/{subId}/monthly", subId)
                        .queryParam("yearMonth", "2026-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.subId").value(subId))
                .andExpect(jsonPath("$.data.yearMonth").value("2026-03"))
                .andExpect(jsonPath("$.data.reports[0].reportId").value(101L))
                .andExpect(jsonPath("$.data.reports[0].title").value("2026년 3월 2주차 분석 리포트"))
                .andExpect(jsonPath("$.data.reports[0].period").value("2026.03.11~2026.03.17"));
    }
}
