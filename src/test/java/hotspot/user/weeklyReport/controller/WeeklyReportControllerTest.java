package hotspot.user.weeklyReport.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
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
import hotspot.user.weeklyReport.controller.response.WeeklyReportResponse;

@WebMvcTest(WeeklyReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class WeeklyReportControllerTest {

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
    @DisplayName("성공: 주간 리포트 상세 조회 시 200 OK와 리포트 데이터를 반환한다")
    void findWeeklyReportSuccess() throws Exception {
        // given
        Long memberId = 1L;
        Long subId = 10L;
        Long reportId = 500L;
        setAuthentication(memberId);

        WeeklyReportResponse response = WeeklyReportResponse.builder()
                .subId(subId)
                .name("자녀 리포트")
                .weekStartDate(LocalDate.of(2026, 3, 9))
                .weekEndDate(LocalDate.of(2026, 3, 15))
                .build();

        given(findWeeklyReportService.findWeeklyReport(memberId, subId, reportId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/ai-reports/families/members/{subId}/reports/{reportId}", subId, reportId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.subId").value(subId))
                .andExpect(jsonPath("$.data.name").value("자녀 리포트"));
    }
}
