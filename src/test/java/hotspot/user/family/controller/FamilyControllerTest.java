package hotspot.user.family.controller;

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

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.family.controller.port.FindFamilyInfoService;
import hotspot.user.family.controller.response.FamilyInfoResponse;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;

/**
 * Family Controller 단위 테스트
 */
@WebMvcTest(FamilyController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FindFamilyInfoService findFamilyInfoService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private void setAuthentication(Long familyId) {
        PrincipalDetails principal = PrincipalDetails.builder()
                .id(1L)
                .email("test@test.com")
                .familyId(familyId)
                .role(FamilyRole.OWNER)
                .status(Status.APPROVED)
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("성공: 가족 정보 조회 API 호출 시 200 OK와 가족 구성원 정보를 반환한다")
    void getFamilyInfoSuccess() throws Exception {
        // given
        Long familyId = 100L;
        setAuthentication(familyId);

        hotspot.user.member.controller.response.MemberResponse member1 = hotspot.user.member.controller.response.MemberResponse.builder()
                .id(1L)
                .name("홍길동")
                .email("test@test.com")
                .familyRole(FamilyRole.OWNER)
                .familyId(familyId)
                .subId(10L)
                .status(Status.APPROVED)
                .build();

        hotspot.user.member.controller.response.MemberResponse member2 = hotspot.user.member.controller.response.MemberResponse.builder()
                .id(2L)
                .name("김철수")
                .email("chulsoo@test.com")
                .familyRole(FamilyRole.CHILD)
                .familyId(familyId)
                .subId(11L)
                .status(Status.APPROVED)
                .build();

        FamilyInfoResponse response = FamilyInfoResponse.builder()
                .familyId(familyId)
                .familyNum(2)
                .memberInfoList(List.of(member1, member2))
                .build();

        given(findFamilyInfoService.findFamilyInfoById(familyId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/families")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.familyId").value(familyId))
                .andExpect(jsonPath("$.data.familyNum").value(2))
                .andExpect(jsonPath("$.data.memberInfoList[0].id").value(1L))
                .andExpect(jsonPath("$.data.memberInfoList[0].name").value("홍길동"))
                .andExpect(jsonPath("$.data.memberInfoList[0].familyRole").value("OWNER"))
                .andExpect(jsonPath("$.data.memberInfoList[1].id").value(2L))
                .andExpect(jsonPath("$.data.memberInfoList[1].name").value("김철수"))
                .andExpect(jsonPath("$.data.memberInfoList[1].familyRole").value("CHILD"));
    }
}
