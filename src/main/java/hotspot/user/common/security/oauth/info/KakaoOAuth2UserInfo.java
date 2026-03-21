package hotspot.user.common.security.oauth.info;

import java.util.Map;

import hotspot.user.member.domain.Provider;

// 사용 안함
/**
 * Kakao OIDC 사용자 정보를 표준 인터페이스로 매핑하는 구현체
 */
public class KakaoOAuth2UserInfo extends OAuth2UserInfo {

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getSocialId() {
        return (String) attributes.get("sub");
    }

    @Override
    public Provider getProvider() {
        return Provider.KAKAO;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("nickname");
    }
}
