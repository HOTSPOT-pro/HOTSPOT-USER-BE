package hotspot.user.common.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import hotspot.user.auth.controller.response.LoginResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.member.controller.port.SocialLoginService;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;

@ExtendWith(MockitoExtension.class)
class CustomOidcUserServiceTest {

    @InjectMocks
    private CustomOidcUserService customOidcUserService;

    @Mock
    private SocialLoginService socialLoginService;

    @Test
    @DisplayName("OIDC 유저 로드 시 SocialLoginService에서 받은 LoginResultDto 정보가 PrincipalDetails에 설정되어야 한다")
    void loadUserShouldSetStatusFromLoginResultDto() {
        // given
        // 1. Mock OidcUserRequest
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("test-client")
                .redirectUri("test-redirect")
                .authorizationUri("test-auth-uri")
                .tokenUri("test-token-uri")
                .build();

        OidcIdToken idToken = new OidcIdToken("token-value", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("sub", "12345", "email", "test@google.com", "name", "Test User"));

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token-value",
                Instant.now(),
                Instant.now().plusSeconds(60));

        OidcUserRequest userRequest = new OidcUserRequest(clientRegistration, accessToken, idToken);

        // 2. Mock SocialLoginService
        LoginResponse loginResult = new LoginResponse(
            1L,
            "test@google.com",
            Status.PENDING,
            FamilyRole.CHILD
        );
        when(socialLoginService.login(any(CreateSocialAccountRequest.class))).thenReturn(loginResult);

        // when
        OidcUser oidcUser = customOidcUserService.loadUser(userRequest);

        // then
        assertThat(oidcUser).isInstanceOf(PrincipalDetails.class);
        PrincipalDetails principalDetails = (PrincipalDetails) oidcUser;
        assertThat(principalDetails.getId()).isEqualTo(loginResult.memberId());
        assertThat(principalDetails.getEmail()).isEqualTo(loginResult.email());
        assertThat(principalDetails.getStatus()).isEqualTo(loginResult.status());
        assertThat(principalDetails.getRole()).isEqualTo(loginResult.familyRole());
    }
}
