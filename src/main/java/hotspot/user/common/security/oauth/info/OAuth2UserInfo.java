package hotspot.user.common.security.oauth.info;

import java.util.Map;

import hotspot.user.member.domain.Provider;

// 사용 안함
/**
 * OAuth2/OIDC 제공자(Google, Kakao 등)의 사용자 정보를 표준화하는 인터페이스
 */
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getSocialId(); // 소셜 고유 식별자
    public abstract Provider getProvider();   // kakao, google 등
    public abstract String getEmail();
    public abstract String getName();

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
