package hotspot.user.common.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import hotspot.user.member.domain.FamilyRole;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PrincipalDetails implements UserDetails, OidcUser {

    private final Long id;
    private final String email;
    private final FamilyRole role;
    private final Map<String, Object> attributes;

    public PrincipalDetails(Long id, String email, FamilyRole role) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.attributes = null;
    }

    public PrincipalDetails(Long id, String email, FamilyRole role, Map<String, Object> attributes) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.attributes = attributes;
    }

    // OidcUser 구현
    @Override
    public Map<String, Object> getClaims() {
        return this.attributes;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return null; // 필요 시 구현
    }

    @Override
    public OidcIdToken getIdToken() {
        return null; // 필요 시 구현
    }

    // OAuth2User 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return email;
    }

    // UserDetails 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
