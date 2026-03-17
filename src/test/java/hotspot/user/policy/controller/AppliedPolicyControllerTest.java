package hotspot.user.policy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;
import hotspot.user.policy.controller.port.FindBlockStatusService;
import hotspot.user.policy.controller.port.FindBlockedTimeService;
import hotspot.user.policy.controller.port.FindFamilyAppliedPolicyService;
import hotspot.user.policy.controller.port.FindMemberAppliedPolicyService;
import hotspot.user.policy.controller.port.UpdatePolicySubService;
import hotspot.user.policy.controller.request.UpdatePolicySubRequest;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.controller.response.BlockedStatusResponse;
import hotspot.user.policy.controller.response.BlockedTimeResponse;
import hotspot.user.policy.controller.response.FamilyAppliedPolicyResponse;
import hotspot.user.policy.controller.response.UpdatePolicySubResponse;

@WebMvcTest(AppliedPolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppliedPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FindMemberAppliedPolicyService findMemberAppliedPolicyService;

    @MockBean
    private FindFamilyAppliedPolicyService findFamilyAppliedPolicyService;

    @MockBean
    private FindBlockStatusService findBlockStatusService;

    @MockBean
    private UpdatePolicySubService updatePolicySubService;

    @MockBean
    private FindBlockedTimeService findBlockedTimeService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private void setAuthentication(FamilyRole role) {
        PrincipalDetails principal = PrincipalDetails.builder()
                .id(1L)
                .email("test@test.com")
                .familyId(100L)
                .role(role)
                .status(Status.APPROVED)
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("본인 적용 정책 조회 성공: 역할에 상관없이 조회 가능하다")
    void getAppliedPoliciesIndividualSuccess() throws Exception {

        setAuthentication(FamilyRole.CHILD);

        AppliedPolicyResponse response = AppliedPolicyResponse.builder()
                .memberId(1L)
                .memberName("자녀")
                .subId(10L)
                .role(FamilyRole.CHILD)
                .familyDataSubLimit(10)
                .familyDataUsage(2)
                .priority(-1)
                .isBlocked(false)
                .blockPolicyResponseList(List.of())
                .appBlockedServiceResponseList(List.of())
                .build();

        given(findMemberAppliedPolicyService.findByMemberId(1L))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/policies/applied")
                        .param("isFamily", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberName").value("자녀"))
                .andExpect(jsonPath("$.data.role").value("CHILD"));
    }

    @Test
    @DisplayName("가족 전체 정책 조회 성공: PARENT 권한일 때")
    void getAppliedPoliciesFamilySuccessByParent() throws Exception {

        setAuthentication(FamilyRole.PARENT);

        FamilyAppliedPolicyResponse response = FamilyAppliedPolicyResponse.builder()
                .familyId(100L)
                .familyNum(1)
                .familyDataAmount(40)
                .priorityType(hotspot.user.family.domain.PriorityType.FIFO)
                .memberPolicies(List.of())
                .build();

        given(findFamilyAppliedPolicyService.findByMemberId(1L))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/policies/applied")
                        .param("isFamily", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.familyId").value(100L));
    }

    @Test
    @DisplayName("가족 전체 정책 조회 실패: CHILD 권한일 때 (ACCESS_DENIED)")
    void getAppliedPoliciesFamilyFailByChild() throws Exception {
        // given
        setAuthentication(FamilyRole.CHILD);

        // when & then
        mockMvc.perform(get("/api/v1/policies/applied")
                        .param("isFamily", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(result -> {
                    assertThat(result.getResolvedException())
                            .isInstanceOf(ApplicationException.class)
                            .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());
                });
    }

    @Test
    @DisplayName("구성원별 정책 업데이트 성공: OWNER 권한일 때")
    void updatePolicySubSuccess() throws Exception {
        // given
        setAuthentication(FamilyRole.OWNER);
        UpdatePolicySubRequest request = new UpdatePolicySubRequest(100L, 1L, List.of(1L, 2L));
        UpdatePolicySubResponse response = UpdatePolicySubResponse.builder()
                .familyId(100L)
                .subId(1L)
                .blockedPolicyIdList(List.of(1L, 2L))
                .build();

        given(updatePolicySubService.updatePolicySub(
                any(UpdatePolicySubRequest.class),
                eq(1L),
                eq(FamilyRole.OWNER)))
                .willReturn(response);

        // when & then
        mockMvc.perform(put("/api/v1/policies/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.familyId").value(100L))
                .andExpect(jsonPath("$.data.blockedPolicyIdList[0]").value(1L));
    }

    @Test
    @DisplayName("나의 데이터 사용 차단 여부 조회 성공")
    void getBlockedStatusSuccess() throws Exception {
        // given
        setAuthentication(FamilyRole.CHILD);
        BlockedStatusResponse response = BlockedStatusResponse.builder()
                .isImmediateBlocked(false)
                .isCurrentlyBlocked(true)
                .blockedPolicies(List.of())
                .build();

        given(findBlockStatusService.findMyBlockStatus(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/policies/blocked")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isCurrentlyBlocked").value(true));
    }

    @Test
    @DisplayName("본인 차단 시간대 조회 성공")
    void getBlockedTimeIndividualSuccess() throws Exception {
        // given
        setAuthentication(FamilyRole.CHILD);
        BlockedTimeResponse response = BlockedTimeResponse.builder()
                .subId(10L)
                .dayBlockedTimes(List.of())
                .build();

        given(findBlockedTimeService.findMemberBlockedTime(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/policies/blockedTime")
                        .param("isFamily", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subId").value(10L));
    }

    @Test
    @DisplayName("가족 차단 시간대 조회 성공")
    void getBlockedTimeFamilySuccess() throws Exception {
        // given
        setAuthentication(FamilyRole.OWNER);
        BlockedTimeResponse response = BlockedTimeResponse.builder()
                .subId(10L)
                .dayBlockedTimes(List.of())
                .build();

        given(findBlockedTimeService.findFamilyBlockedTime(1L)).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/policies/blockedTime")
                        .param("isFamily", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].subId").value(10L));
    }

    @Test
    @DisplayName("가족 차단 시간대 조회 실패: CHILD 권한일 때 (ACCESS_DENIED)")
    void getBlockedTimeFamilyFailByChild() throws Exception {
        // given
        setAuthentication(FamilyRole.CHILD);

        given(findBlockedTimeService.findFamilyBlockedTime(1L))
                .willThrow(new ApplicationException(AuthErrorCode.ACCESS_DENIED));

        // when & then
        mockMvc.perform(get("/api/v1/policies/blockedTime")
                        .param("isFamily", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(result -> {
                    assertThat(result.getResolvedException())
                            .isInstanceOf(ApplicationException.class)
                            .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());
                });
    }
}
