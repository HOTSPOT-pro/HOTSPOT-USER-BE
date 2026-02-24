package hotspot.user.usage.subscriptionUsage.controller;

import static hotspot.user.util.TestSecurityUtil.setAuthentication;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
import hotspot.user.usage.subscriptionUsage.controller.port.FindSubscriptionUsageService;
import hotspot.user.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;

@WebMvcTest(controllers = SubscriptionUsageController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubscriptionUsageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FindSubscriptionUsageService findSubscriptionUsageService;

    @MockBean
    JwtFilter jwtFilter;

    @MockBean
    JwtProvider jwtProvider;

    @MockBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("개인 데이터 사용량 조회 성공")
    void shouldReturnSubscriptionUsageSuccessfully() throws Exception {

        setAuthentication(1L, 1L, FamilyRole.OWNER);

        LocalDateTime now = LocalDateTime.now();

        SubscriptionUsageResponse response =
                new SubscriptionUsageResponse(
                        1L,
                        now,
                        24.0,
                        0.0,
                        24.0,
                        0,
                        4.0,
                        0.5,
                        3.5,
                        13,
                        List.of(
                                new SubscriptionUsageResponse.GiftUsageResponse(
                                        69395L,
                                        "김태연",
                                        1.0,
                                        0.5,
                                        0.5,
                                        50
                                )
                        )
                );

        when(findSubscriptionUsageService.findSubscriptionUsage(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/subscriptionUsage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subId").value(1L))
                .andExpect(jsonPath("$.data.subDataAmount").value(24.0))
                .andExpect(jsonPath("$.data.giftDataAmount").value(4.0))
                .andExpect(jsonPath("$.data.giftUsages[0].giftId").value(69395L))
                .andExpect(jsonPath("$.data.giftUsages[0].giftUserName").value("김태연"));
    }
}
