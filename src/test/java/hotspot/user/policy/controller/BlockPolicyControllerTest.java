package hotspot.user.policy.controller;

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
import org.springframework.test.web.servlet.MockMvc;

import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.policy.controller.port.FindBlockPolicyService;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.domain.PolicyType;

/**
 * 정책 Controller 테스트 코드
 */

@WebMvcTest(BlockPolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class BlockPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FindBlockPolicyService findBlockPolicyService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("전체 정책 목록 조회 API 성공")
    void getAllPoliciesSuccess() throws Exception {
        // given
        BlockPolicyResponse response = BlockPolicyResponse.builder()
                .id(1L)
                .name("기본 정책")
                .policyType(PolicyType.ONCE)
                .build();

        given(findBlockPolicyService.findAll()).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].name").value("기본 정책"))
                .andExpect(jsonPath("$.data[0].policyType").value("ONCE"));
    }
}
