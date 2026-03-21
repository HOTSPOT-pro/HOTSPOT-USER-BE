package hotspot.user.policy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
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
import hotspot.user.policy.controller.port.CreateBlockPolicyService;
import hotspot.user.policy.controller.port.DeleteFamilyBlockPolicyService;
import hotspot.user.policy.controller.port.FindBlockPolicyService;
import hotspot.user.policy.controller.port.FindFamilyBlockPolicyService;
import hotspot.user.policy.controller.port.FindSingleBlockPolicyService;
import hotspot.user.policy.controller.port.UpdateBlockPolicyService;
import hotspot.user.policy.controller.port.UpdateFamilyBlockPolicyStatusService;
import hotspot.user.policy.controller.request.BlockPolicyRequest;
import hotspot.user.policy.controller.request.UpdateFamilyBlockPolicyStatusRequest;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.controller.response.UpdateFamilyBlockPolicyStatusResponse;
import hotspot.user.policy.domain.PolicySnapshot;
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
    private FindSingleBlockPolicyService findSingleBlockPolicyService;

    @MockBean
    private CreateBlockPolicyService createBlockPolicyService;

    @MockBean
    private UpdateBlockPolicyService updateBlockPolicyService;

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
        BlockPolicyResponse policy = BlockPolicyResponse.builder()
                .id(1L)
                .name("Admin Policy")
                .policyType(PolicyType.SCHEDULED)
                .isActive(true)
                .build();
        given(findBlockPolicyService.findAll()).willReturn(List.of(policy));

        mockMvc.perform(get("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }

    @Test
    @DisplayName("성공: 우리 가족 정책 목록 조회 시 200 OK와 목록을 반환한다")
    void getFamilyPoliciesSuccess() throws Exception {
        BlockPolicyResponse policy = BlockPolicyResponse.builder()
                .id(10L)
                .familyId(FAMILY_ID)
                .name("우리 가족 정책")
                .isActive(true)
                .build();
        given(findFamilyBlockPolicyService.findAllByFamilyId(anyLong(), anyLong()))
                .willReturn(List.of(policy));

        mockMvc.perform(get("/api/v1/policies/families")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].familyId").value(FAMILY_ID));
    }

    @Test
    @DisplayName("성공: 정책 단일 조회 시 200 OK와 결과를 반환한다")
    void getBlockPolicySuccess() throws Exception {
        BlockPolicyResponse policy = BlockPolicyResponse.builder()
                .id(1L)
                .name("정책")
                .familyId(FAMILY_ID)
                .isActive(true)
                .build();
        given(findSingleBlockPolicyService.find(anyLong(), anyLong(), anyLong()))
                .willReturn(policy);

        mockMvc.perform(get("/api/v1/policies/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("성공: 우리 가족 정책 상태 업데이트 API 호출 시 200 OK를 반환한다")
    void updateFamilyBlockPoliciesSuccess() throws Exception {
        List<Long> activeIds = List.of(1L, 2L);
        UpdateFamilyBlockPolicyStatusRequest request = new UpdateFamilyBlockPolicyStatusRequest(
                FAMILY_ID, activeIds);
        UpdateFamilyBlockPolicyStatusResponse mockResponse = UpdateFamilyBlockPolicyStatusResponse.builder()
                .familyId(FAMILY_ID)
                .blockedPolicyIdList(activeIds)
                .build();

        given(updateFamilyBlockPolicyStatusService.updateFamilyBlockPolicyStatus(
                any(UpdateFamilyBlockPolicyStatusRequest.class), anyLong(), anyLong()))
                .willReturn(mockResponse);

        mockMvc.perform(patch("/api/v1/policies/families")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.familyId").value(FAMILY_ID));
    }

    @Test
    @DisplayName("성공: 우리 가족 정책 생성 API 호출 시 201 Created를 반환한다")
    void createBlockPolicySuccess() throws Exception {
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .days(List.of(DayOfWeek.MONDAY))
                .startTime("09:00")
                .endTime("18:00")
                .build();
        BlockPolicyRequest request = new BlockPolicyRequest(
                "새 정책", PolicyType.SCHEDULED, snapshot, "설명", true);
        BlockPolicyResponse response = BlockPolicyResponse.builder()
                .id(1L)
                .name("새 정책")
                .build();

        given(createBlockPolicyService.create(any(BlockPolicyRequest.class), anyLong(), anyLong()))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("성공: 우리 가족 정책 수정 API 호출 시 200 OK를 반환한다")
    void updateBlockPolicySuccess() throws Exception {
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .durationMinutes(30)
                .build();
        BlockPolicyRequest request = new BlockPolicyRequest(
                "수정 이름", PolicyType.ONCE, snapshot, "수정 설명", false);
        BlockPolicyResponse response = BlockPolicyResponse.builder()
                .id(1L)
                .name("수정 이름")
                .build();

        given(updateBlockPolicyService.update(any(BlockPolicyRequest.class), anyLong(), anyLong(), anyLong()))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/policies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정 이름"));
    }

    @Test
    @DisplayName("성공: 우리 가족 정책 삭제 API 호출 시 200 OK를 반환한다")
    void deleteFamilyBlockPoliciesSuccess() throws Exception {
        doNothing().when(deleteFamilyBlockPolicyService).delete(anyList(), anyLong(), anyLong());

        mockMvc.perform(delete("/api/v1/policies/families")
                        .param("policyIdList", "1", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
