package hotspot.user.usage.familyUsage.controller;

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
import hotspot.user.usage.familyUsage.controller.port.FindFamilyUsageService;
import hotspot.user.usage.familyUsage.controller.response.FamilyUsageResponse;

@WebMvcTest(controllers = FamilyUsageController.class)
@AutoConfigureMockMvc(addFilters = false)   // 🔥 필터 끄고 직접 세팅
class FamilyUsageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FindFamilyUsageService findFamilyUsageService;

    @MockBean
    JwtFilter jwtFilter;

    @MockBean
    JwtProvider jwtProvider;

    @MockBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("가족 공용 데이터 조회 성공")
    void shouldReturnFamilyUsageSuccessfully() throws Exception {

        setAuthentication(1L, 1L, FamilyRole.OWNER);

        FamilyUsageResponse response =
                new FamilyUsageResponse(
                        20.0,
                        5.0,
                        15.0,
                        25,
                        List.of()
                );

        when(findFamilyUsageService.findFamilyUsage(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/familyUsage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.familyDataAmount")
                        .value(20.0));
    }
}
