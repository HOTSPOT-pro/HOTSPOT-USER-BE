package hotspot.user.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.oauth.CustomOidcUserService;
import hotspot.user.common.security.oauth.OAuth2FailureHandler;
import hotspot.user.common.security.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtFilter jwtFilter;
  private final CustomOidcUserService customOidcUserService;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final OAuth2FailureHandler oAuth2FailureHandler;
  private final CorsConfig corsConfig;


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
      .csrf(AbstractHttpConfigurer::disable) // csrf 비활성화
      .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 방식 비활성화
      .formLogin(FormLoginConfigurer::disable) // 기본 로그인 비활성화
      .httpBasic(AbstractHttpConfigurer::disable); // Basic 인증 비활성화 : Basic 인증은 사용자 이름 & 비밀번호를 Base64로 인코딩하여 인증값으로 활용

    // UsernamePasswordAuthenticationFilter : 이 클래스에서 폼 로그인 인증을 처리
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    http.oauth2Login(oauth -> oauth
                    .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler));


    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(
            "/", "/health", "/login/**", "/oauth2/**", "/oauth/**",
            "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health"
        ).permitAll()
        .anyRequest().authenticated()
    );


    return http.build();
  }
}
