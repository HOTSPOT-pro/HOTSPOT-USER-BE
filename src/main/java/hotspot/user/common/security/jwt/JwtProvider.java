package hotspot.user.common.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.member.domain.FamilyRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 JWT 토큰 생성, 검증 및 Authentication 객체 변환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private SecretKey key;

    // 토큰 발행시 액세스 / 리프레시 토큰 구분을 위해 claim에 토큰 타입 명시하기 위해 추가
    private static final String TYPE_CLAIM = "type";
    private static final String ROLE_CLAIM = "role";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    // 시크릿 키 String -> SecretKey로 변환
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Authentication authentication) {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        return createToken(authentication, principal, jwtProperties.getAccessExpiration(),
                ACCESS_TOKEN_TYPE);
    }

    public String createRefreshToken(Authentication authentication) {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        return createToken(authentication, principal, jwtProperties.getRefreshExpiration(),
                REFRESH_TOKEN_TYPE);
    }

    private String createToken(Authentication authentication, PrincipalDetails principal,
                               long expirationTime, String type) {
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + expirationTime);

        var builder = Jwts.builder()
                .header().add("typ", "JWT").and()
                .claim("memberId", principal.getId())
                .claim(TYPE_CLAIM, type) // 토큰 타입 명시
                .claim(ROLE_CLAIM, principal.getRole().name()) // 모든 토큰에 권한 정보 포함 (재발급 시 필요)
                .subject(authentication.getName())
                .issuedAt(now)
                .expiration(expiredDate)
                .signWith(key)
                .id(UUID.randomUUID().toString());

        return builder.compact();
    }

    // 토큰에서 인증 정보 추출 (Access Token 전용)
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        // 1. 토큰 타입 검증
        String type = claims.get(TYPE_CLAIM, String.class);
        if (!ACCESS_TOKEN_TYPE.equals(type)) {
            throw new MalformedJwtException("토큰 타입이 올바르지 않습니다. Expected: ACCESS, Actual: " + type);
        }

        return getAuthenticationFromClaims(claims, token);
    }

    // Refresh Token에서 인증 정보 추출 (재발급 전용)
    public Authentication getAuthenticationFromRefreshToken(String token) {
        Claims claims = getClaims(token);

        // 1. 토큰 타입 검증
        String type = claims.get(TYPE_CLAIM, String.class);
        if (!REFRESH_TOKEN_TYPE.equals(type)) {
            throw new MalformedJwtException("토큰 타입이 올바르지 않습니다. Expected: REFRESH, Actual: " + type);
        }

        return getAuthenticationFromClaims(claims, token);
    }

    private Authentication getAuthenticationFromClaims(Claims claims, String token) {
        String email = claims.getSubject();
        Long memberId = claims.get("memberId", Long.class);
        String roleStr = claims.get(ROLE_CLAIM, String.class);

        // 2. 권한 정보 검증 (기본값 제거)
        if (roleStr == null) {
            throw new MalformedJwtException("권한 정보가 없는 토큰입니다.");
        }

        PrincipalDetails principal = new PrincipalDetails(memberId, email, FamilyRole.valueOf(roleStr));

        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    // Request Header에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰 유효성 검증 (서명 및 만료)
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    // 토큰 파싱
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
