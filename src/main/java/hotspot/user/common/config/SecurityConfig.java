package hotspot.user.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import hotspot.user.common.security.jwt.JwtAccessDeniedHandler;
import hotspot.user.common.security.jwt.JwtAuthenticationEntryPoint;
import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.oauth.CustomOidcUserService;
import hotspot.user.common.security.oauth.OAuth2FailureHandler;
import hotspot.user.common.security.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 유저 Status 상수화
    // 로그인 & PENDING 상태인지 확인하는 로직
    private static final String IS_PENDING_STATUS =
            "isAuthenticated() and principal.status == T(hotspot.user.member.domain.Status).PENDING";

    // 로그인 & APPROVED 상태인지 확인하는 로직
    private static final String IS_APPROVED_STATUS =
            "isAuthenticated() and principal.status == T(hotspot.user.member.domain.Status).APPROVED";

    private final JwtFilter jwtFilter;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final CorsConfig corsConfig;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable) // csrf 비활성화
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 방식 비활성화
                .formLogin(FormLoginConfigurer::disable) // 기본 로그인 비활성화
                // Basic 인증 비활성화 : Basic 인증은 사용자 이름 & 비밀번호를 Base64로 인코딩하여 인증값으로 활용
                .httpBasic(AbstractHttpConfigurer::disable);

        // Filter에서 에러 핸들링 대신 커스텀 에러 핸들링 추가
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401 처리
                .accessDeniedHandler(jwtAccessDeniedHandler)         // 403 처리
        );

        // UsernamePasswordAuthenticationFilter : 이 클래스에서 폼 로그인 인증을 처리
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        http.oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oAuth2FailureHandler));


        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/health", "/error", "/error/**", "/login/**", "/oauth2/**", "/oauth/**",
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                        "/actuator/health", "/api/v1/auth/reissue"
                ).permitAll()
                // isAuthenticated() 조건 추가로 500 에러 방지
                // 온보딩은 PENDING인 유저만 가능
                .requestMatchers("/api/v1/auth/onboarding")
                .access(new WebExpressionAuthorizationManager(IS_PENDING_STATUS))
                // 나머지 비즈니스 API는 반드시 APPROVED 상태의 로그인한 유저만 접근 가능
                .requestMatchers("/api/v1/**")
                .access(new WebExpressionAuthorizationManager(IS_APPROVED_STATUS))
                .anyRequest().authenticated()
        );


        return http.build();
    }
}
