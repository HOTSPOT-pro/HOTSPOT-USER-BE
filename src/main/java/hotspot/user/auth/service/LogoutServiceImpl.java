package hotspot.user.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.auth.controller.port.LogoutService;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.service.port.TokenRepository;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.dispatch.sse.registry.SseEmitterRegistry;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LogoutServiceImpl implements LogoutService {

    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;
    private final SubscriptionRepository subscriptionRepository;
    private final SseEmitterRegistry sseEmitterRegistry;

    @Override
    public void logout(Long memberId, TokenRequest request) {
        String refreshToken = request.refreshToken();

        // 1. 토큰 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }

        // 2. 토큰에서 사용자 정보 추출 및 소유권 검증
        PrincipalDetails principal = (PrincipalDetails) jwtProvider.getAuthenticationFromRefreshToken(refreshToken)
                .getPrincipal();

        if (!memberId.equals(principal.getId())) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }

        // 3. 토큰 삭제 (무효화)
        tokenRepository.deleteByMemberId(memberId);
        subscriptionRepository.findByMemberId(memberId)
                .ifPresent(subscription -> sseEmitterRegistry.closeAllBySubId(subscription.getId()));
    }
}
