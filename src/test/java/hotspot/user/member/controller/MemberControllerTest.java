package hotspot.user.member.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.controller.port.FindMemberService;
import hotspot.user.member.controller.response.MemberResponse;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;

/**
 * Member Controller 단위 테스트
 */
@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FindMemberService findMemberService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private void setAuthentication(Long memberId) {
        PrincipalDetails principal = PrincipalDetails.builder()
                .id(memberId)
                .email("test@test.com")
                .role(FamilyRole.OWNER)
                .status(Status.APPROVED)
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("성공: 내 정보 조회 API 호출 시 200 OK와 사용자 정보를 반환한다")
    void getMemberInfoSuccess() throws Exception {
        // given
        Long memberId = 1L;
        setAuthentication(memberId);

        MemberResponse response = MemberResponse.builder()
                .id(memberId)
                .name("홍길동")
                .email("test@test.com")
                .familyRole(FamilyRole.OWNER)
                .status(Status.APPROVED)
                .build();

        given(findMemberService.findById(memberId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"));
    }
}
