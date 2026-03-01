package hotspot.user.policy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;
import hotspot.user.policy.controller.port.DeleteFamilyBlockPolicyService;
import hotspot.user.policy.controller.port.FindBlockPolicyService;
import hotspot.user.policy.controller.port.FindFamilyBlockPolicyService;
import hotspot.user.policy.controller.port.UpdateFamilyBlockPolicyStatusService;
import hotspot.user.policy.controller.request.UpdateFamilyBlockPolicyStatusRequest;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.controller.response.UpdateFamilyBlockPolicyStatusResponse;
import hotspot.user.policy.domain.PolicyType;

@WebMvcTest(BlockPolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class BlockPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FindBlockPolicyService findBlockPolicyService;

    @MockBean
    private FindFamilyBlockPolicyService findFamilyBlockPolicyService;

    @MockBean
    private UpdateFamilyBlockPolicyStatusService updateFamilyBlockPolicyStatusService;

    @MockBean
    private DeleteFamilyBlockPolicyService deleteFamilyBlockPolicyService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final Long MEMBER_ID = 1L;
    private static final Long FAMILY_ID = 100L;

    @BeforeEach
    void setUp() {
        // PrincipalDetails Mocking 설정 (SecurityContext에 저장)
        PrincipalDetails principal = new PrincipalDetails(
                MEMBER_ID,
                "test@email.com",
                FAMILY_ID,
                FamilyRole.OWNER,
                Status.APPROVED);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("성공: 전체 정책 목록 조회 시 200 OK와 목록을 반환한다")
    void getAllPoliciesSuccess() throws Exception {
        // given
        BlockPolicyResponse policy = BlockPolicyResponse.builder()
                .id(1L)
                .name("Admin Policy")
                .policyType(PolicyType.SCHEDULED)
                .isActive(true)
                .build();
        given(findBlockPolicyService.findAll()).willReturn(List.of(policy));

        // when & then
        mockMvc.perform(get("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Admin Policy"));
    }

    @Test
    @DisplayName("성공: 우리 가족 정책 목록 조회 시 200 OK와 목록을 반환한다")
    void getFamilyPoliciesSuccess() throws Exception {
        // given
        BlockPolicyResponse policy = BlockPolicyResponse.builder()
                .id(10L)
                .name("Family Policy")
                .familyId(FAMILY_ID)
                .policyType(PolicyType.SCHEDULED)
                .isActive(true)
                .build();
        given(findFamilyBlockPolicyService.findAllByFamilyId(anyLong(), anyLong()))
                .willReturn(List.of(policy));

        // when & then
        mockMvc.perform(get("/api/v1/policies/families")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(10L))
                .andExpect(jsonPath("$.data[0].familyId").value(FAMILY_ID))
                .andExpect(jsonPath("$.data[0].name").value("Family Policy"));
    }

    @Test
    @DisplayName("성공: 우리 가족 정책 상태 업데이트 API 호출 시 200 OK와 결과를 반환한다")
    void updateFamilyBlockPoliciesSuccess() throws Exception {
        // given
        List<Long> activeIds = List.of(1L, 2L);
        UpdateFamilyBlockPolicyStatusRequest request = new UpdateFamilyBlockPolicyStatusRequest(FAMILY_ID, activeIds);
        UpdateFamilyBlockPolicyStatusResponse mockResponse = UpdateFamilyBlockPolicyStatusResponse.builder()
                .familyId(FAMILY_ID)
                .blockedPolicyIdList(activeIds)
                .build();

        given(updateFamilyBlockPolicyStatusService.updateFamilyBlockPolicyStatus(
                any(UpdateFamilyBlockPolicyStatusRequest.class), anyLong(), anyLong()))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(patch("/api/v1/policies/families")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.familyId").value(FAMILY_ID))
                .andExpect(jsonPath("$.data.blockedPolicyIdList").isArray())
                .andExpect(jsonPath("$.data.blockedPolicyIdList[0]").value(1L))
                .andExpect(jsonPath("$.data.blockedPolicyIdList[1]").value(2L));
    }

    @Test
    @DisplayName("실패: 필수 파라미터(familyId)가 누락된 경우 400 에러를 반환한다")
    void updateFamilyBlockPoliciesFailNullFamilyId() throws Exception {
        // given
        UpdateFamilyBlockPolicyStatusRequest request = new UpdateFamilyBlockPolicyStatusRequest(null, List.of(1L));

        // when & then
        mockMvc.perform(patch("/api/v1/policies/families")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공: 우리 가족 정책 삭제 API 호출 시 200 OK를 반환한다")
    void deleteFamilyBlockPoliciesSuccess() throws Exception {
        // given
        List<Long> policyIds = List.of(1L, 2L);
        doNothing().when(deleteFamilyBlockPolicyService).delete(anyList(), anyLong(), anyLong());

        // when & then
        mockMvc.perform(delete("/api/v1/policies")
                        .param("policyIdList", "1", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
