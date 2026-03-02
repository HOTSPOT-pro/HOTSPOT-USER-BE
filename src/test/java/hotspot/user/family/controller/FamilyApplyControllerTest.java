package hotspot.user.family.controller;

import static hotspot.user.util.TestSecurityUtil.setAuthentication;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import hotspot.user.family.controller.port.AddFamilyMemberService;
import hotspot.user.family.controller.port.CreateFamilyApplyService;
import hotspot.user.family.controller.request.AddFamilyMemberRequest;
import hotspot.user.family.controller.request.CreateFamilyApplyRequest;
import hotspot.user.family.controller.request.FamilyMemberRequest;
import hotspot.user.family.controller.response.AddFamilyMemberResponse;
import hotspot.user.family.controller.response.CreateFamilyApplyResponse;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.member.domain.FamilyRole;

@WebMvcTest(FamilyApplyController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilyApplyControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CreateFamilyApplyService createFamilyApplyService;
    @MockBean
    private AddFamilyMemberService addFamilyMemberService;
    @MockBean
    private JwtFilter jwtFilter;
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

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

    @Test
    @DisplayName("성공: 가족 구성원 추가 신청 API 호출 시 200 OK를 반환한다")
    void addFamilyMemberSuccess() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        FamilyMemberRequest memberReq = new FamilyMemberRequest("홍길동", "01011112222", FamilyRole.CHILD);
        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.ADD, "doc-url", List.of(memberReq));

        AddFamilyMemberResponse response = AddFamilyMemberResponse.builder()
                .familyId(100L)
                .applyType(ApplyType.ADD)
                .build();

        given(addFamilyMemberService.addFamilyMember(eq(1L), eq(100L), any(AddFamilyMemberRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/families/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.familyId").value(100L));
    }

    @Test
    @DisplayName("실패: 다건 추가 신청 시 전화번호 형식이 올바르지 않으면 400 에러를 반환한다")
    void addFamilyMemberFailInvalidPhone() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        FamilyMemberRequest memberReq = new FamilyMemberRequest("홍길동", "010-123-456", FamilyRole.CHILD);
        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.ADD, "doc-url", List.of(memberReq));

        // when & then
        mockMvc.perform(post("/api/v1/families/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패: 다건 추가 신청 시 필수 필드 누락 시 400 에러를 반환한다")
    void addFamilyMemberFailMissingField() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        AddFamilyMemberRequest request = new AddFamilyMemberRequest(null, "doc-url", List.of()); // Type 누락 + 리스트 비어있음

        // when & then
        mockMvc.perform(post("/api/v1/families/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
