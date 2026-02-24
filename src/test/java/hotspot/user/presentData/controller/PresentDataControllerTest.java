package hotspot.user.presentData.controller;

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
import hotspot.user.presentData.controller.port.FindFamilyDataService;
import hotspot.user.presentData.controller.response.FamilyDataResponse;

@WebMvcTest(controllers = PresentDataController.class)
@AutoConfigureMockMvc(addFilters = false)
class PresentDataControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FindFamilyDataService findFamilyDataService;

    @MockBean
    JwtFilter jwtFilter;

    @MockBean
    JwtProvider jwtProvider;

    @MockBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("가족 선물 데이터 조회 성공")
    void shouldReturnFamilyPresentDataSuccessfully() throws Exception {

        // 🔥 Security Context 직접 세팅
        setAuthentication(1L, 10L, FamilyRole.OWNER);

        FamilyDataResponse.SubUsageResponse sub1 =
                new FamilyDataResponse.SubUsageResponse(
                        2L,
                        "신진훈",
                        -1.0,
                        0.0,
                        0
                );

        FamilyDataResponse.SubUsageResponse sub2 =
                new FamilyDataResponse.SubUsageResponse(
                        3L,
                        "김태연",
                        24.0,
                        3.0,
                        13
                );

        FamilyDataResponse response =
                new FamilyDataResponse(
                        1L,
                        24.0,
                        List.of(sub1, sub2)
                );

        when(findFamilyDataService.findFamilyData(1L, 10L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/presentData"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.selfSubId").value(1))
                .andExpect(jsonPath("$.data.selfDataRemainAmount").value(24.0))
                .andExpect(jsonPath("$.data.subUsages[0].subId").value(2))
                .andExpect(jsonPath("$.data.subUsages[0].subDataLimitAmount").value(-1.0))
                .andExpect(jsonPath("$.data.subUsages[1].dataUsagePercent").value(13));
    }
}
