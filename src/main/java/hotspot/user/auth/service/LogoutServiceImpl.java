package hotspot.user.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.auth.controller.port.LogoutService;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.service.port.TokenRepository;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LogoutServiceImpl implements LogoutService {

    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;

    @Override
    public void logout(TokenRequest request) {
        String refreshToken = request.refreshToken();

        if (jwtProvider.validateToken(refreshToken)) {
            PrincipalDetails principal = (PrincipalDetails) jwtProvider.getAuthentication(refreshToken).getPrincipal();
            tokenRepository.deleteByMemberId(principal.getId());
        }
    }
}
