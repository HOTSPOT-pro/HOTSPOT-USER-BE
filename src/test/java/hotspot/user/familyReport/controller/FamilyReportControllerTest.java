package hotspot.user.familyReport.controller;

import static hotspot.user.util.TestSecurityUtil.setAuthentication;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import hotspot.user.familyReport.controller.port.FindFamilyReportSubscriptionService;
import hotspot.user.familyReport.controller.response.FamilyReportSubscriptionResponse;
import hotspot.user.member.domain.FamilyRole;

@WebMvcTest(FamilyReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilyReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FindFamilyReportSubscriptionService findFamilyReportSubscriptionService;

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
}
