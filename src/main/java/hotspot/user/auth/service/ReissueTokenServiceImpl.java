package hotspot.user.auth.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.auth.controller.port.ReissueTokenService;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.auth.domain.Token;
import hotspot.user.auth.service.port.TokenRepository;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.GlobalErrorCode;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReissueTokenServiceImpl implements ReissueTokenService {

    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;

    @Override
    public TokenResponse reissue(TokenRequest request) {
        String refreshToken = request.refreshToken();

        // 1. 토큰 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new ApplicationException(GlobalErrorCode.BAD_REQUEST); // 공통 에러 코드로 변경
        }

        // 2. 인증 객체 추출
        Authentication authentication = jwtProvider.getAuthentication(refreshToken);
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();

        // 3. Redis에 저장된 Refresh Token 확인
        Token savedToken = tokenRepository.findByMemberId(principal.getId())
                .orElseThrow(() -> new ApplicationException(GlobalErrorCode.BAD_REQUEST)); // 공통 에러 코드로 변경

        if (!savedToken.getRefreshToken().equals(refreshToken)) {
            throw new ApplicationException(GlobalErrorCode.BAD_REQUEST); // 공통 에러 코드로 변경
        }

        // 4. 새로운 토큰 생성 (RTR)
        String newAccessToken = jwtProvider.createAccessToken(authentication);
        String newRefreshToken = jwtProvider.createRefreshToken(authentication);

        // 5. Redis 토큰 업데이트
        tokenRepository.save(Token.builder()
                .memberId(principal.getId())
                .refreshToken(newRefreshToken)
                .build());

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
