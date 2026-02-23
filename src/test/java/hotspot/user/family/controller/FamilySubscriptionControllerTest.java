package hotspot.user.family.controller;

import static hotspot.user.util.TestSecurityUtil.setAuthentication;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.family.controller.port.CreateFamilyApplyService;
import hotspot.user.family.controller.port.UpdateDataLimitService;
import hotspot.user.family.controller.port.UpdateFamilyPriorityService;
import hotspot.user.family.controller.port.UpdateFamilyRoleService;
import hotspot.user.family.controller.request.MemberPriorityRequest;
import hotspot.user.family.controller.request.UpdateDataLimitRequest;
import hotspot.user.family.controller.request.UpdateFamilyPriorityRequest;
import hotspot.user.family.controller.request.UpdateFamilyRoleRequest;
import hotspot.user.family.controller.response.MemberPriorityResponse;
import hotspot.user.family.controller.response.UpdateDataLimitResponse;
import hotspot.user.family.controller.response.UpdateFamilyPriorityResponse;
import hotspot.user.family.controller.response.UpdateFamilyRoleResponse;
import hotspot.user.family.domain.PriorityType;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;

/**
 * FamilySubscription Controller 단위 테스트
 */
@WebMvcTest(FamilySubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilySubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UpdateDataLimitService updateDataLimitService;

    @MockBean
    private UpdateFamilyPriorityService updateFamilyPriorityService;

    @MockBean
    private UpdateFamilyRoleService updateFamilyRoleService;

    @MockBean
    private FamilySubscriptionRepository familySubscriptionRepository;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("성공: OWNER 권한을 가진 사용자가 데이터 한도를 수정하면 200 OK를 반환한다")
    void updateDataLimitSuccess() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        UpdateDataLimitRequest request = new UpdateDataLimitRequest(100L, 1L, 5000);
        UpdateDataLimitResponse response = UpdateDataLimitResponse.builder()
                .familyId(100L)
                .subId(1L)
                .dataLimit(5000)
                .build();

        given(updateDataLimitService.updateDataLimit(any(UpdateDataLimitRequest.class), eq(100L), eq(FamilyRole.OWNER)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/families/data-limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.dataLimit").value(5000));
    }

    @Test
    @DisplayName("실패: CHILD 권한을 가진 사용자가 데이터 한도를 수정하려 하면 403 에러가 발생한다")
    void updateDataLimitFailByChild() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.CHILD);
        UpdateDataLimitRequest request = new UpdateDataLimitRequest(100L, 1L, 5000);

        // 서비스가 호출되기 전 컨트롤러의 권한 체크 로직에서 예외가 발생함
        given(updateDataLimitService.updateDataLimit(any(UpdateDataLimitRequest.class), eq(100L), eq(FamilyRole.CHILD)))
                .willThrow(new ApplicationException(AuthErrorCode.ACCESS_DENIED));

        // when & then
        mockMvc.perform(patch("/api/v1/families/data-limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(result -> {
                    assertThat(result.getResolvedException())
                            .isInstanceOf(ApplicationException.class)
                            .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());
                });
    }

    @Test
    @DisplayName("성공: OWNER 권한을 가진 사용자가 가족 우선순위를 수정하면 200 OK를 반환한다")
    void updateFamilyPrioritySuccess() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        List<MemberPriorityRequest> memberPriorities = List.of(
                new MemberPriorityRequest(1L, 1),
                new MemberPriorityRequest(2L, 2)
        );
        UpdateFamilyPriorityRequest request = new UpdateFamilyPriorityRequest(
                100L,
                PriorityType.PRIORITY,
                memberPriorities);

        List<MemberPriorityResponse> memberPriorityResponses = List.of(
                MemberPriorityResponse.builder().subId(1L).priority(1).build(),
                MemberPriorityResponse.builder().subId(2L).priority(2).build()
        );
        UpdateFamilyPriorityResponse response = UpdateFamilyPriorityResponse.builder()
                .familyId(100L)
                .priorityType(PriorityType.PRIORITY)
                .memberPriorities(memberPriorityResponses)
                .build();

        given(updateFamilyPriorityService.updateFamilyPriority(any(UpdateFamilyPriorityRequest.class),
                eq(100L),
                eq(FamilyRole.OWNER)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/families/priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.priorityType").value("PRIORITY"))
                .andExpect(jsonPath("$.data.memberPriorities[0].subId").value(1L));
    }

    @Test
    @DisplayName("실패: CHILD 권한을 가진 사용자가 가족 우선순위를 수정하려 하면 403 에러가 발생한다")
    void updateFamilyPriorityFailByChild() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.CHILD);
        UpdateFamilyPriorityRequest request = new UpdateFamilyPriorityRequest(
                100L,
                PriorityType.FIFO,
                null);

        given(updateFamilyPriorityService.updateFamilyPriority(any(UpdateFamilyPriorityRequest.class),
                eq(100L),
                eq(FamilyRole.CHILD)))
                .willThrow(new ApplicationException(AuthErrorCode.ACCESS_DENIED));

        // when & then
        mockMvc.perform(patch("/api/v1/families/priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(result -> {
                    assertThat(result.getResolvedException())
                            .isInstanceOf(ApplicationException.class)
                            .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());
                });
    }

    @Test
    @DisplayName("실패: 우선순위 모드일 때 memberPriorities가 비어있으면 400 에러를 반환한다")
    void updateFamilyPriorityFailByEmptyMemberPriorities() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        UpdateFamilyPriorityRequest request = new UpdateFamilyPriorityRequest(
                100L,
                PriorityType.PRIORITY,
                null);

        given(updateFamilyPriorityService.updateFamilyPriority(any(UpdateFamilyPriorityRequest.class),
                eq(100L),
                eq(FamilyRole.OWNER)))
                .willThrow(new ApplicationException(FamilyErrorCode.MISSING_PRIORITY_VALUES));

        // when & then
        mockMvc.perform(patch("/api/v1/families/priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FAMILY_009"));
    }

    @Test
    @DisplayName("성공: OWNER 권한으로 구성원의 역할을 수정하면 200 OK를 반환한다")
    void updateFamilyRoleSuccess() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        UpdateFamilyRoleRequest request = new UpdateFamilyRoleRequest(FamilyRole.PARENT);
        UpdateFamilyRoleResponse response = UpdateFamilyRoleResponse.builder()
                .familyId(100L)
                .subId(2L)
                .familyRole(FamilyRole.PARENT)
                .build();

        given(updateFamilyRoleService.update(eq(1L), eq(100L), eq(FamilyRole.OWNER), eq(2L), any(UpdateFamilyRoleRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/families/members/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.familyRole").value("PARENT"));
    }

    @Test
    @DisplayName("실패: OWNER 본인의 역할을 수정하려 하면 400 에러가 발생한다")
    void updateFamilyRoleFailBySelf() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        UpdateFamilyRoleRequest request = new UpdateFamilyRoleRequest(FamilyRole.PARENT);

        given(updateFamilyRoleService.update(eq(1L), eq(100L), eq(FamilyRole.OWNER), eq(10L), any(UpdateFamilyRoleRequest.class)))
                .willThrow(new ApplicationException(FamilyErrorCode.CANNOT_CHANGE_OWNER_ROLE));

        // when & then
        mockMvc.perform(patch("/api/v1/families/members/10/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FAMILY_016"));
    }

    @Test
    @DisplayName("실패: 타인을 OWNER로 변경하려 하면 400 에러가 발생한다")
    void updateFamilyRoleFailToAssignOwner() throws Exception {
        // given
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        UpdateFamilyRoleRequest request = new UpdateFamilyRoleRequest(FamilyRole.OWNER);

        given(updateFamilyRoleService.update(eq(1L), eq(100L), eq(FamilyRole.OWNER), eq(2L), any(UpdateFamilyRoleRequest.class)))
                .willThrow(new ApplicationException(FamilyErrorCode.CANNOT_ASSIGN_OWNER_ROLE));

        // when & then
        mockMvc.perform(patch("/api/v1/families/members/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FAMILY_017"));
    }
}
