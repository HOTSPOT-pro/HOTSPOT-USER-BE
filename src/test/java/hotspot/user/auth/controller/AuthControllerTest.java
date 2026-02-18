package hotspot.user.auth.controller;

import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hotspot.user.auth.controller.port.LogoutService;
import hotspot.user.auth.controller.port.OnboardingService;
import hotspot.user.auth.controller.port.ReissueTokenService;
import hotspot.user.auth.controller.request.OnboardingRequest;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProperties;
import hotspot.user.common.security.jwt.JwtProvider;

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
    private JwtProperties jwtProperties;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    // JPA Auditing 에러 방지용 Mock Bean 추가
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final String ACCESS_TOKEN = "access-token-example";
    private static final String REFRESH_TOKEN = "refresh-token-example";
    private static final long COOKIE_EXPIRATION = 604800000L;

    @BeforeEach
    void setUp() {
        given(jwtProperties.getRefreshExpiration()).willReturn(COOKIE_EXPIRATION);
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
        OnboardingRequest request = createDummyOnboardingRequest();
        TokenResponse mockResponse = new TokenResponse(ACCESS_TOKEN, REFRESH_TOKEN);

        given(onboardingService.onboarding(any(OnboardingRequest.class))).willReturn(mockResponse);

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
        doNothing().when(logoutService).logout(any(TokenRequest.class));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(requestCookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("Max-Age=0")));

        verify(logoutService).logout(any(TokenRequest.class));
    }

    private OnboardingRequest createDummyOnboardingRequest() {
        try {
            return new OnboardingRequest(1L, "test@email.com", "010-1234-5678", "900101-1");
        } catch (Exception e) {
            return null;
        }
    }
}
