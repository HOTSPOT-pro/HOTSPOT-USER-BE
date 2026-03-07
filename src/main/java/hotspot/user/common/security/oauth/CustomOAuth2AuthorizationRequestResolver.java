package hotspot.user.common.security.oauth;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * OAuth2 로그인 시 소셜 서비스의 세션을 무시하고 로그인 창을 강제하기 위한 리졸버
 * 로그아웃
 * 
 * [파라미터 설정]
 * - 구글: prompt=select_account (항상 계정 선택창 노출)
 * - 카카오: prompt=login (기존 세션이 있어도 다시 로그인 창 노출)
 */
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        // 기본 리졸버를 생성하여 위임 구조로 구성 (엔드포인트는 기본값인 /oauth2/authorization 사용)
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(authorizationRequest);
    }

    /**
     * 기존 인증 요청에 소셜 서비스별 커스텀 파라미터를 추가함.
     */
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            return null;
        }

        // 기존 파라미터를 복사하여 새로운 맵 생성
        Map<String, Object> additionalParameters = new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
        
        // 현재 요청 중인 소셜 서비스 식별 (google, kakao 등)
        String registrationId = (String) authorizationRequest.getAttribute("registration_id");

        // 서비스별 prompt 파라미터 추가
        if ("google".equals(registrationId)) {
            additionalParameters.put("prompt", "select_account");
        } else if ("kakao".equals(registrationId)) {
            additionalParameters.put("prompt", "select_account");
        }

        // 변경된 파라미터를 포함하여 새로운 인증 요청 객체 빌드
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }
}
