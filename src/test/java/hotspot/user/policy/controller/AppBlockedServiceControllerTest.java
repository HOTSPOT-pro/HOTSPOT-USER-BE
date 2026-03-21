package hotspot.user.policy.controller;

import static hotspot.user.util.TestSecurityUtil.setAuthentication;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.port.FindAppBlockedService;
import hotspot.user.policy.controller.port.UpdateAppBlockedServiceService;
import hotspot.user.policy.controller.request.UpdateAppBlockedServiceRequest;
import hotspot.user.policy.controller.response.AppBlockedServiceResponse;
import hotspot.user.policy.controller.response.UpdateAppBlockedServiceResponse;

/**
 * 앱 차단 서비스 Controller 테스트 코드
 */
@WebMvcTest(AppBlockedServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppBlockedServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FindAppBlockedService findAppBlockedService;

    @MockBean
    private UpdateAppBlockedServiceService updateAppBlockedServiceService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("앱 차단 서비스 전체 목록 조회 API 성공")
    void getAllAppBlockedServicesSuccess() throws Exception {
        // given
        AppBlockedServiceResponse response = AppBlockedServiceResponse.builder()
                .id(1L)
                .name("YouTube")
                .serviceCode("YOUTUBE")
                .build();

        given(findAppBlockedService.findAll()).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/blocking")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].name").value("YouTube"))
                .andExpect(jsonPath("$.data[0].serviceCode").value("YOUTUBE"));
    }

    @Test
    @DisplayName("성공: OWNER 권한으로 앱 차단 설정을 업데이트한다")
    void updateAppBlockedServiceSuccess() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        UpdateAppBlockedServiceRequest request = new UpdateAppBlockedServiceRequest(1L, 100L, List.of(1L, 2L));
        UpdateAppBlockedServiceResponse response = new UpdateAppBlockedServiceResponse(1L, 100L, List.of(1L, 2L));

        given(updateAppBlockedServiceService.updateAppBlockedService(any(), eq(100L), eq(FamilyRole.OWNER)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/blocking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subId").value(100L))
                .andExpect(jsonPath("$.data.blockedServiceIdList").isArray());
    }
}
