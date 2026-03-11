package hotspot.user.usage.totalUsage.controller;

import static hotspot.user.util.TestSecurityUtil.setAuthentication;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

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
import hotspot.user.usage.totalUsage.controller.port.FindTotalUsageService;
import hotspot.user.usage.totalUsage.controller.response.TotalUsageResponse;

@WebMvcTest(controllers = TotalUsageController.class)
@AutoConfigureMockMvc(addFilters = false)
class TotalUsageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FindTotalUsageService findTotalUsageService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("전체 데이터 사용량 조회 성공")
    void shouldReturnTotalUsageSuccessfully() throws Exception {

        setAuthentication(1L, 2L, FamilyRole.OWNER);

        TotalUsageResponse response =
                new TotalUsageResponse(
                        7L,
                        LocalDateTime.now(),
                        38.0,
                        31.0,
                        82,
                        "5G 프리미엄",
                        17.0,
                        6.0,
                        8.0
                );

        when(findTotalUsageService.findTotalUsage(1L, 2L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/totalUsage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subId").value(7L))
                .andExpect(jsonPath("$.data.planName").value("5G 프리미엄"))
                .andExpect(jsonPath("$.data.totalDataAmount").value(38.0))
                .andExpect(jsonPath("$.data.totalDataRemainAmount").value(31.0))
                .andExpect(jsonPath("$.data.totalDataRemainPercent").value(82))
                .andExpect(jsonPath("$.data.subDataRemainAmount").value(17.0))
                .andExpect(jsonPath("$.data.giftDataRemainAmount").value(6.0))
                .andExpect(jsonPath("$.data.familyDataRemainAmount").value(8.0));
    }
}
