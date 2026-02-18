package hotspot.user.auth.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.auth.controller.port.IssueTokenService;
import hotspot.user.auth.controller.port.SaveTokenService;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import lombok.RequiredArgsConstructor;

/**
 * IssueTokenService 구현체
 * 토큰 생성, 저장 및 응답 반환 책임을 담당
 */
@Service
@RequiredArgsConstructor
@Transactional
public class IssueTokenServiceImpl implements IssueTokenService {

    private final JwtProvider jwtProvider;
    private final SaveTokenService saveTokenService;

    @Override
    public TokenResponse issue(Member member, String email, FamilyRole familyRole) {
        // 인증 객체 생성
        PrincipalDetails principal = PrincipalDetails.builder()
                .id(member.getId())
                .email(email)
                .role(familyRole)
                .status(member.getStatus())
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal, null, principal.getAuthorities()
        );

        // 토큰 발행
        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(authentication);

        // 토큰 저장 (Redis)
        saveTokenService.saveToken(member.getId(), new TokenRequest(refreshToken));

        return new TokenResponse(accessToken, refreshToken);
    }
}
