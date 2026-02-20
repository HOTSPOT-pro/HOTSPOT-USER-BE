package hotspot.user.policy.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;
import hotspot.user.policy.controller.port.FindFamilyAppliedPolicyService;
import hotspot.user.policy.controller.port.FindMemberAppliedPolicyService;
import hotspot.user.policy.controller.response.AppliedPolicyResponse;
import hotspot.user.policy.controller.response.FamilyAppliedPolicyResponse;

@WebMvcTest(AppliedPolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppliedPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FindMemberAppliedPolicyService findMemberAppliedPolicyService;

    @MockBean
    private FindFamilyAppliedPolicyService findFamilyAppliedPolicyService;

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
        // given
        setAuthentication(FamilyRole.CHILD);
        AppliedPolicyResponse response = AppliedPolicyResponse.builder()
                .memberId(1L)
                .memberName("자녀")
                .build();

        given(findMemberAppliedPolicyService.findByMemberId(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/policies/applied")
                        .param("isFamily", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberName").value("자녀"));
    }

    @Test
    @DisplayName("가족 전체 정책 조회 성공: PARENT 권한일 때")
    void getAppliedPoliciesFamilySuccessByParent() throws Exception {
        // given
        setAuthentication(FamilyRole.PARENT);
        FamilyAppliedPolicyResponse response = FamilyAppliedPolicyResponse.builder()
                .familyId(100L)
                .memberPolicies(List.of())
                .build();

        given(findFamilyAppliedPolicyService.findByFamilyId(100L)).willReturn(response);

        // when & then
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
    @DisplayName("임시 테스트 API 조회 성공: 파라미터 기반 조회 확인")
    void getAppliedPoliciesTestApiSuccess() throws Exception {
        // given
        AppliedPolicyResponse response = AppliedPolicyResponse.builder()
                .memberId(1L)
                .memberName("테스트유저")
                .build();
        given(findMemberAppliedPolicyService.findByMemberId(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/policies/applied/test")
                        .param("testMemberId", "1")
                        .param("isFamily", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberName").value("테스트유저"));
    }
}
