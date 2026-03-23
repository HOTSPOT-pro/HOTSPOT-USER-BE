package hotspot.user.common.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private final String secret;
    private final Long accessExpiration;
    private final Long refreshExpiration;
    private final Long onboardingExpiration;

    // 환경 변수 주입 없이 코드에서 관리하는 상수
    private final String accessTokenName = "userAccessToken";
    private final String refreshTokenName = "userRefreshToken";

}
