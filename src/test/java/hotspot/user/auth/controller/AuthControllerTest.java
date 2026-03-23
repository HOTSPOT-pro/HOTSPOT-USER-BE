package hotspot.user.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.auth.controller.port.GetMemberInfoService;
import hotspot.user.auth.controller.port.IssueTokenService;
import hotspot.user.auth.controller.port.LogoutService;
import hotspot.user.auth.controller.port.OnboardingService;
import hotspot.user.auth.controller.port.ReissueTokenService;
import hotspot.user.auth.controller.port.WithdrawService;
import hotspot.user.auth.controller.request.OnboardingRequest;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.controller.response.MemberInfoResponse;
import hotspot.user.auth.controller.response.OnboardingResponse;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.auth.controller.swagger.AuthApi;
import hotspot.user.common.ApiResponse;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProperties;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReissueTokenService reissueTokenService;

    @MockBean
    private IssueTokenService issueTokenService;

    @MockBean
    private LogoutService logoutService;

    @MockBean
    private OnboardingService onboardingService;

    @MockBean
    private WithdrawService withdrawService;

    @MockBean
    private GetMemberInfoService getMemberInfoService;

    @MockBean
    private JwtProperties jwtProperties;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final String ACCESS_TOKEN = "access-token-example";
    private static final String REFRESH_TOKEN = "refresh-token-example";
    private static final long COOKIE_EXPIRATION = 604800000L;
    private static final Long MEMBER_ID = 1L;
    private static final String EMAIL = "test@email.com";

    @BeforeEach
    void setUp() {
        given(jwtProperties.getRefreshExpiration()).willReturn(COOKIE_EXPIRATION);
        given(jwtProperties.getAccessExpiration()).willReturn(3600000L);

        // PrincipalDetails Mocking 설정 (SecurityContext에 저장)
        PrincipalDetails principal = new PrincipalDetails(
                MEMBER_ID,
                EMAIL,
                100L,
                FamilyRole.PARENT, Status.APPROVED);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("토큰 재발급(reissue) 성공 시 AccessToken과 RefreshToken이 모두 Cookie에 담겨야 한다")
    void reissueSuccess() throws Exception {
        Cookie requestCookie = new Cookie(JwtProperties.REFRESH_TOKEN_NAME, "old-refresh-token");
        TokenResponse mockResponse = new TokenResponse(ACCESS_TOKEN, REFRESH_TOKEN);

        given(reissueTokenService.reissue(any(TokenRequest.class))).willReturn(mockResponse);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/reissue")
                        .cookie(requestCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(ACCESS_TOKEN))
                .andReturn();

        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).anyMatch(c -> c.contains(JwtProperties.ACCESS_TOKEN_NAME + "=" + ACCESS_TOKEN));
        assertThat(cookies).anyMatch(c -> c.contains(JwtProperties.REFRESH_TOKEN_NAME + "=" + REFRESH_TOKEN));
    }

    @Test
    @DisplayName("온보딩(onboarding) 성공 시 OnboardingResponse와 토큰 쿠키들을 반환한다")
    void onboardingSuccess() throws Exception {
        OnboardingRequest request = new OnboardingRequest("01012345678", "900101");
        TokenResponse tokenResponse = new TokenResponse(ACCESS_TOKEN, REFRESH_TOKEN);
        OnboardingResponse mockResponse = OnboardingResponse.builder()
                .tokenResponse(tokenResponse)
                .name("test")
                .email(EMAIL)
                .build();

        given(onboardingService.onboarding(anyLong(), anyString(), any(OnboardingRequest.class)))
                .willReturn(mockResponse);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).anyMatch(c -> c.contains(JwtProperties.ACCESS_TOKEN_NAME + "=" + ACCESS_TOKEN));
        assertThat(cookies).anyMatch(c -> c.contains(JwtProperties.REFRESH_TOKEN_NAME + "=" + REFRESH_TOKEN));
    }

    @Test
    @DisplayName("온보딩(onboarding) 실패: 잘못된 전화번호 형식일 경우 400 에러를 반환한다")
    void onboardingFailInvalidPhone() throws Exception {
        OnboardingRequest request = new OnboardingRequest("010-123-456", "900101");

        mockMvc.perform(post("/api/v1/auth/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그아웃(logout) 성공 시 AccessToken과 RefreshToken 쿠키를 모두 삭제해야 한다")
    void logoutSuccess() throws Exception {
        Cookie requestCookie = new Cookie(JwtProperties.REFRESH_TOKEN_NAME, REFRESH_TOKEN);
        doNothing().when(logoutService).logout(anyLong(), any(TokenRequest.class));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(requestCookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).anyMatch(c -> c.contains(JwtProperties.ACCESS_TOKEN_NAME + "=") && c.contains("Max-Age=0"));
        assertThat(cookies).anyMatch(c -> c.contains(JwtProperties.REFRESH_TOKEN_NAME + "=") && c.contains("Max-Age=0"));
    }

    @Test
    @DisplayName("로그아웃(logout) 실패: 리프레시 토큰 쿠키가 없으면 에러를 던진다")
    void logoutFailNoCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원탈퇴(withdraw) 성공 시 AccessToken과 RefreshToken 쿠키를 모두 삭제해야 한다")
    void withdrawSuccess() throws Exception {
        Cookie requestCookie = new Cookie(JwtProperties.REFRESH_TOKEN_NAME, REFRESH_TOKEN);
        doNothing().when(withdrawService).withdraw(anyLong(), any(TokenRequest.class));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/withdraw")
                        .cookie(requestCookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).anyMatch(c -> c.contains(JwtProperties.ACCESS_TOKEN_NAME + "=") && c.contains("Max-Age=0"));
        assertThat(cookies).anyMatch(c -> c.contains(JwtProperties.REFRESH_TOKEN_NAME + "=") && c.contains("Max-Age=0"));
    }

    @Test
    @DisplayName("내 정보 조회(getInfo) 성공 시 상세 정보를 반환한다")
    void getInfoSuccess() throws Exception {
        MemberInfoResponse mockResponse = MemberInfoResponse.builder()
                .name("test")
                .email(EMAIL)
                .familyRole(FamilyRole.PARENT)
                .familyId(100L)
                .build();

        given(getMemberInfoService.getMemberInfo(anyLong(), anyString())).willReturn(mockResponse);

        mockMvc.perform(get("/api/v1/auth/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("test"))
                .andExpect(jsonPath("$.data.email").value(EMAIL));
    }
}
