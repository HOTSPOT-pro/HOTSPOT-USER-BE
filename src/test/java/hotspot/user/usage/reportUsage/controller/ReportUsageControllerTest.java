package hotspot.user.usage.reportUsage.controller;

import static hotspot.user.util.TestSecurityUtil.setAuthentication;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.usage.reportUsage.controller.port.FindReportFamilyService;
import hotspot.user.usage.reportUsage.controller.port.FindReportUsageAppDayService;
import hotspot.user.usage.reportUsage.controller.port.FindReportUsageAppMonthService;
import hotspot.user.usage.reportUsage.controller.port.FindReportUsageDayService;
import hotspot.user.usage.reportUsage.controller.port.FindReportUsageMonthService;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageAppResponse;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageDayResponse;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageMonthResponse;

@WebMvcTest(controllers = ReportUsageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportUsageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FindReportFamilyService findReportFamilyService;
    @MockBean FindReportUsageAppMonthService findReportUsageAppMonthService;
    @MockBean
    FindReportUsageAppDayService findReportUsageAppDayService;
    @MockBean
    FindReportUsageDayService findReportUsageDayService;
    @MockBean
    FindReportUsageMonthService findReportUsageMonthService;
    @MockBean JwtFilter jwtFilter;
    @MockBean JwtProvider jwtProvider;
    @MockBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("가족 일별 데이터 조회 성공")
    void shouldReturnFamilyDailyUsageSuccessfully() throws Exception {

        setAuthentication(1L, 1L, FamilyRole.OWNER);

        ReportUsageDayResponse response =
                new ReportUsageDayResponse(
                        LocalDateTime.now(),
                        List.of(
                                new ReportUsageDayResponse.SubUsageResponse(
                                        -1L,
                                        "가족 전체",
                                        List.of(
                                                new ReportUsageDayResponse
                                                        .SubUsageResponse
                                                        .DataUsageDayResponse(
                                                        LocalDate.of(2026, 2, 23),
                                                        8.5
                                                )
                                        )
                                )
                        )
                );

        when(findReportUsageDayService
                .findReportUsageDay(1L, null, YearMonth.of(2026, 2)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/reportUsage/day")
                        .param("month", "2026-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subUsages[0].subName")
                        .value("가족 전체"));
    }

    @Test
    @DisplayName("가족 월별 데이터 조회 성공")
    void shouldReturnFamilyMonthlyUsageSuccessfully() throws Exception {

        setAuthentication(1L, 1L, FamilyRole.OWNER);

        ReportUsageMonthResponse response =
                new ReportUsageMonthResponse(
                        LocalDateTime.now(),
                        List.of(
                                new ReportUsageMonthResponse.SubUsageMonthResponse(
                                        -1L,
                                        "가족 전체",
                                        List.of(
                                                new ReportUsageMonthResponse
                                                        .SubUsageMonthResponse
                                                        .DataUsageMonthResponse(
                                                        YearMonth.of(2026, 2),
                                                        9.5
                                                )
                                        )
                                )
                        )
                );

        when(findReportUsageMonthService
                .findReportUsageMonth(1L, null))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/reportUsage/month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subUsages[0].subName")
                        .value("가족 전체"));
    }

    @Test
    @DisplayName("가족 월별 조회 - targetSubId 포함 성공")
    void shouldReturnFamilyMonthlyUsageWithTarget() throws Exception {

        setAuthentication(1L, 1L, FamilyRole.OWNER);

        ReportUsageMonthResponse response =
                new ReportUsageMonthResponse(
                        LocalDateTime.now(),
                        List.of(
                                new ReportUsageMonthResponse.SubUsageMonthResponse(
                                        2L,
                                        "신진훈",
                                        List.of(
                                                new ReportUsageMonthResponse
                                                        .SubUsageMonthResponse
                                                        .DataUsageMonthResponse(
                                                        YearMonth.of(2026, 2),
                                                        3.0
                                                )
                                        )
                                )
                        )
                );

        when(findReportUsageMonthService
                .findReportUsageMonth(1L, 2L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/reportUsage/month")
                        .param("targetSubId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subUsages[0].subId")
                        .value(2L));
    }

    @Test
    @DisplayName("앱 월별 사용량 조회 성공")
    void shouldReturnAppMonthlyUsageSuccessfully() throws Exception {

        setAuthentication(1L, 1L, FamilyRole.OWNER);

        ReportUsageAppResponse response =
                new ReportUsageAppResponse(
                        LocalDateTime.now(),
                        List.of(
                                new ReportUsageAppResponse.AppUsageResponse(
                                        1L,
                                        "YouTube",
                                        3.5
                                )
                        )
                );

        when(findReportUsageAppMonthService
                .findReportUsageAppMonth(1L, 10L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/reportUsage/app/month")
                        .param("targetSubId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.appUsages[0].appName")
                        .value("YouTube"))
                .andExpect(jsonPath("$.data.appUsages[0].appDataUsageAmount")
                        .value(3.5));
    }

    @Test
    @DisplayName("앱 일별 사용량 조회 성공")
    void shouldReturnAppDailyUsageSuccessfully() throws Exception {

        setAuthentication(1L, 1L, FamilyRole.OWNER);

        ReportUsageAppResponse response =
                new ReportUsageAppResponse(
                        LocalDateTime.now(),
                        List.of(
                                new ReportUsageAppResponse.AppUsageResponse(
                                        2L,
                                        "Instagram",
                                        2.0
                                )
                        )
                );

        when(findReportUsageAppDayService
                .findReportUsageAppDay(1L, 10L, LocalDate.now()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/reportUsage/app/day")
                        .param("targetSubId", "10")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.appUsages[0].appName")
                        .value("Instagram"))
                .andExpect(jsonPath("$.data.appUsages[0].appDataUsageAmount")
                        .value(2.0));
    }
}
