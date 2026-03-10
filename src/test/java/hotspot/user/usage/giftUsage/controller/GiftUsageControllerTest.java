package hotspot.user.usage.giftUsage.controller;

import static hotspot.user.util.TestSecurityUtil.setAuthentication;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import hotspot.user.usage.giftUsage.controller.port.FindGiftUsageService;
import hotspot.user.usage.giftUsage.controller.response.GiftUsageListResponse;

@WebMvcTest(controllers = GiftUsageController.class)
@AutoConfigureMockMvc(addFilters = false)
class GiftUsageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FindGiftUsageService findGiftUsageService;

    @MockBean
    JwtFilter jwtFilter;

    @MockBean
    JwtProvider jwtProvider;

    @MockBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("선물 데이터 사용량 조회 성공")
    void shouldReturnGiftUsageSuccessfully() throws Exception {

        setAuthentication(1L, 1L, FamilyRole.OWNER);

        GiftUsageListResponse response =
                new GiftUsageListResponse(
                        10.0,
                        3.0,
                        7.0,
                        70,
                        List.of(
                                new GiftUsageListResponse.GiftUsageResponse(
                                        101L,
                                        "김태연",
                                        5.0,
                                        1.0,
                                        4.0,
                                        80
                                )
                        )
                );

        when(findGiftUsageService.findGiftUsages(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/giftUsage/gifts")
                        .param("memberId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.giftDataAmount").value(10.0))
                .andExpect(jsonPath("$.data.giftDataUsageAmount").value(3.0))
                .andExpect(jsonPath("$.data.giftDataRemainAmount").value(7.0))
                .andExpect(jsonPath("$.data.giftRemainPercent").value(70))
                .andExpect(jsonPath("$.data.giftUsages[0].giftId").value(101L))
                .andExpect(jsonPath("$.data.giftUsages[0].giftUserName").value("김태연"));
    }
}
