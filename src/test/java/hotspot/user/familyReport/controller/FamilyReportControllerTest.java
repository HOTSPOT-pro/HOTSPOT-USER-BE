package hotspot.user.familyReport.controller;

import static hotspot.user.util.TestSecurityUtil.setAuthentication;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import hotspot.user.familyReport.controller.port.CancelFamilyReportSubscriptionService;
import hotspot.user.familyReport.controller.port.CreateFamilyReportSubscriptionService;
import hotspot.user.familyReport.controller.port.FindFamilyReportSubscriptionService;
import hotspot.user.familyReport.controller.port.UpdateFamilyReportReceiveDayService;
import hotspot.user.familyReport.controller.request.CreateFamilyReportSubscriptionRequest;
import hotspot.user.familyReport.controller.request.UpdateFamilyReportReceiveDayRequest;
import hotspot.user.familyReport.controller.response.FamilyReportSubscriptionResponse;
import hotspot.user.member.domain.FamilyRole;

@WebMvcTest(FamilyReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilyReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CancelFamilyReportSubscriptionService cancelFamilyReportSubscriptionService;

    @MockBean
    private CreateFamilyReportSubscriptionService createFamilyReportSubscriptionService;

    @MockBean
    private FindFamilyReportSubscriptionService findFamilyReportSubscriptionService;

    @MockBean
    private UpdateFamilyReportReceiveDayService updateFamilyReportReceiveDayService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("가족 AI 리포트 구독 여부 조회 성공")
    void findFamilyReportSubscriptionSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);

        given(findFamilyReportSubscriptionService.findSubscription(100L))
                .willReturn(FamilyReportSubscriptionResponse.builder()
                        .subscribed(true)
                        .build());

        mockMvc.perform(get("/api/v1/ai-reports/families"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.subscribed").value(true));
    }

    @Test
    @DisplayName("미구독 상태 조회 성공")
    void findFamilyReportSubscriptionUnsubscribed() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.CHILD);

        given(findFamilyReportSubscriptionService.findSubscription(100L))
                .willReturn(FamilyReportSubscriptionResponse.unsubscribed());

        mockMvc.perform(get("/api/v1/ai-reports/families"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subscribed").value(false));
    }

    @Test
    @DisplayName("가족 AI 리포트 구독 신청 성공")
    void createFamilyReportSubscriptionSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);

        CreateFamilyReportSubscriptionRequest request =
                new CreateFamilyReportSubscriptionRequest(java.time.DayOfWeek.TUESDAY);

        doNothing().when(createFamilyReportSubscriptionService)
                .createSubscription(100L, FamilyRole.OWNER, any(CreateFamilyReportSubscriptionRequest.class));

        mockMvc.perform(post("/api/v1/ai-reports/families")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("가족 AI 리포트 수신 요일 변경 성공")
    void updateFamilyReportReceiveDaySuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);

        UpdateFamilyReportReceiveDayRequest request =
                new UpdateFamilyReportReceiveDayRequest(java.time.DayOfWeek.THURSDAY);

        doNothing().when(updateFamilyReportReceiveDayService)
                .updateReceiveDay(100L, FamilyRole.OWNER, any(UpdateFamilyReportReceiveDayRequest.class));

        mockMvc.perform(patch("/api/v1/ai-reports/families/receive-day")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("가족 AI 리포트 구독 취소 성공")
    void cancelFamilyReportSubscriptionSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);

        doNothing().when(cancelFamilyReportSubscriptionService)
                .cancelSubscription(100L, FamilyRole.OWNER);

        mockMvc.perform(delete("/api/v1/ai-reports/families"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
