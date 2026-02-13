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
import hotspot.user.member.domain.SocialAccount;
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
        // ID Token 가져오기 (Spring Security가 이미 서명 검증 및 유효성 체크를 완료한 상태)
        OidcIdToken idToken = userRequest.getIdToken();
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // ID Token 내부의 정보(Claims) 추출
        Map<String, Object> claims = idToken.getClaims();

        // 로그: 실제 들어온 클레임 확인용
        log.debug("[OIDC] ID Token Claims: {}", claims);

        String email = null;
        String name = null;
        String socialId = idToken.getSubject(); // sub claim

        // Provider별 Claim 파싱 (표준 OIDC 및 카카오 커스텀)
        if ("kakao".equals(registrationId)) {
            email = (String) claims.get("email");
            name = (String) claims.get("nickname");
        } else if ("google".equals(registrationId)) {
            // 구글: 표준 클레임 사용
            email = (String) claims.get("email");
            name = (String) claims.get("name");
        } else {
            // 기타 OIDC Provider (표준 따름)
            email = (String) claims.get("email");
            name = (String) claims.get("name"); // name claim 추가 확인
        }

        // 필수 정보 누락 체크
        if (email == null) {
            log.error("[OIDC] ID Token에 이메일이 없습니다. Provider 설정을 확인하세요. Claims: {}", claims);
            throw new OAuth2AuthenticationException("ID Token에 이메일 정보가 누락되었습니다.");
        }

        // 비즈니스 로직 (회원가입/갱신)
        Provider provider = Provider.valueOf(registrationId.toUpperCase());

        CreateSocialAccountRequest request = new CreateSocialAccountRequest(
                name,
                email,
                socialId,
                provider,
                null // memberId는 서비스 내부에서 처리
        );

        Member member = socialLoginService.login(request);

        log.info("[OIDC] 로그인 성공: provider={}, email={}, memberId={}", registrationId, email, member.getId());

        // 5. PrincipalDetails 반환 (DB에서 가져온 실제 Member 정보 사용)
        return new PrincipalDetails(
                member.getId(),
                member.getSocialAccountList().stream().findFirst().map(SocialAccount::getEmail).orElse(null),
                FamilyRole.CHILD, // [To-Do] 추후 수정 필요
                claims // attributes 자리에 claims 저장
        );
    }
}
