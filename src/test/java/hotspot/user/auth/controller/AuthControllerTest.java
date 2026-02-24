package hotspot.user.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;

import org.hamcrest.Matchers;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.auth.controller.port.LogoutService;
import hotspot.user.auth.controller.port.OnboardingService;
import hotspot.user.auth.controller.port.ReissueTokenService;
import hotspot.user.auth.controller.port.WithdrawService;
import hotspot.user.auth.controller.request.OnboardingRequest;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.controller.response.TokenResponse;
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
    private LogoutService logoutService;

    @MockBean
    private OnboardingService onboardingService;

    @MockBean
    private WithdrawService withdrawService; //  추가

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

    @BeforeEach
    void setUp() {
        given(jwtProperties.getRefreshExpiration()).willReturn(COOKIE_EXPIRATION);

        // PrincipalDetails Mocking 설정 (SecurityContext에 저장)
        PrincipalDetails principal = new PrincipalDetails(
                MEMBER_ID,
                "test@email.com",
                100L,
                FamilyRole.PARENT, Status.APPROVED);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("토큰 재발급(reissue) 성공 시 AccessToken은 Body에, RefreshToken은 Cookie에 담겨야 한다")
    void reissueSuccess() throws Exception {
        Cookie requestCookie = new Cookie("refreshToken", "old-refresh-token");
        TokenResponse mockResponse = new TokenResponse(ACCESS_TOKEN, REFRESH_TOKEN);

        given(reissueTokenService.reissue(any(TokenRequest.class))).willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .cookie(requestCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(ACCESS_TOKEN))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        Matchers.containsString("refreshToken=" + REFRESH_TOKEN)));
    }

    @Test
    @DisplayName("온보딩(onboarding) 성공 시 AccessToken과 RefreshToken 쿠키를 반환한다")
    void onboardingSuccess() throws Exception {
        OnboardingRequest request = new OnboardingRequest("01012345678", "900101");
        TokenResponse mockResponse = new TokenResponse(ACCESS_TOKEN, REFRESH_TOKEN);

        given(onboardingService.onboarding(anyLong(), anyString(), any(OnboardingRequest.class)))
                .willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(ACCESS_TOKEN))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        Matchers.containsString("refreshToken=" + REFRESH_TOKEN)));
    }

    @Test
    @DisplayName("로그아웃(logout) 성공 시 RefreshToken 쿠키를 삭제(Max-Age=0)해야 한다")
    void logoutSuccess() throws Exception {
        Cookie requestCookie = new Cookie("refreshToken", REFRESH_TOKEN);
        doNothing().when(logoutService).logout(anyLong(), any(TokenRequest.class));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(requestCookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("Max-Age=0")));

        verify(logoutService).logout(anyLong(), any(TokenRequest.class));
    }

    @Test
    @DisplayName("회원탈퇴(withdraw) 성공 시 RefreshToken 쿠키를 삭제하고 상태를 반환한다")
    void withdrawSuccess() throws Exception {
        Cookie requestCookie = new Cookie("refreshToken", REFRESH_TOKEN);
        doNothing().when(withdrawService).withdraw(anyLong(), any(TokenRequest.class));

        mockMvc.perform(post("/api/v1/auth/withdraw")
                        .cookie(requestCookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("Max-Age=0")));

        verify(withdrawService).withdraw(anyLong(), any(TokenRequest.class));
    }
}
