package hotspot.user.family.controller;

import static hotspot.user.util.TestSecurityUtil.setAuthentication;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import hotspot.user.family.controller.port.CreateFamilyApplyService;
import hotspot.user.family.controller.request.CreateFamilyApplyRequest;
import hotspot.user.family.controller.response.CreateFamilyApplyResponse;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.member.domain.FamilyRole;

@WebMvcTest(FamilyApplyController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilyApplyControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CreateFamilyApplyService createFamilyApplyService;
    @MockBean private JwtFilter jwtFilter;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("성공: 가족 신청 생성 API 호출 시 200 OK를 반환한다")
    void manageFamilyMemberSuccess() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        CreateFamilyApplyRequest request = CreateFamilyApplyRequest.builder()
                .targetSubId(2L)
                .applyType(ApplyType.ADD)
                .targetFamilyRole(FamilyRole.CHILD)
                .docUrl("http://doc.url")
                .build();

        CreateFamilyApplyResponse response = CreateFamilyApplyResponse.builder()
                .status(hotspot.user.family.domain.ApplyStatus.PENDING)
                .build();

        given(createFamilyApplyService.manage(
                eq(1L), eq(100L), eq(FamilyRole.OWNER), any(CreateFamilyApplyRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/families")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }
}
