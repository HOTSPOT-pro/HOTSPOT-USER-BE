package hotspot.user.common.security.oauth;

import java.util.Map;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.member.controller.port.SocialLoginService;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OIDC ID Token만을 사용하여 로그인 처리 (UserInfo API 호출 최적화)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final SocialLoginService socialLoginService;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. ID Token 파싱 및 검증
        OidcIdToken idToken = userRequest.getIdToken();
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> claims = idToken.getClaims();

        log.debug("[OIDC] ID Token Claims: {}", claims);

        // 2. 사용자 정보 추출
        CreateSocialAccountRequest request = extractUserInfo(registrationId, claims, idToken.getSubject());

        // 3. 비즈니스 로직 수행 (로그인/회원가입)
        return processLogin(request, claims);
    }

    private CreateSocialAccountRequest extractUserInfo(String registrationId, Map<String, Object> claims, String socialId) {
        String email;
        String name;

        // Provider별 Claim 파싱
        if ("kakao".equals(registrationId)) {
            email = (String) claims.get("email");
            name = (String) claims.get("nickname");
        } else {
            // Google 및 표준 OIDC
            email = (String) claims.get("email");
            name = (String) claims.get("name");
        }

        validateAttributes(email, claims);

        return new CreateSocialAccountRequest(
            name,
            email,
            socialId,
            Provider.valueOf(registrationId.toUpperCase()),
            null
        );
    }

    private void validateAttributes(String email, Map<String, Object> claims) {
        if (email == null) {
            log.error("[OIDC] ID Token에 이메일이 없습니다. Provider 설정을 확인하세요. Claims: {}", claims);
            throw new OAuth2AuthenticationException("ID Token에 이메일 정보가 누락되었습니다.");
        }
    }

    private PrincipalDetails processLogin(CreateSocialAccountRequest request, Map<String, Object> claims) {
        Member member = socialLoginService.login(request);

        log.info("[OIDC] 로그인 성공: provider={}, email={}, memberId={}", request.provider(), request.email(), member.getId());

        return new PrincipalDetails(
            member.getId(),
            member.getSocialAccount().getEmail(),
            FamilyRole.CHILD, // [To-Do] 추후 회선 추가 시 수정 필요
            claims
        );
    }
}
