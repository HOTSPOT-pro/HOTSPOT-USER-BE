package hotspot.user.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.auth.controller.port.SaveTokenService;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.domain.Token;
import hotspot.user.auth.service.port.TokenRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SaveTokenServiceImpl implements SaveTokenService {

    private final TokenRepository tokenRepository;

    @Override
    public void saveToken(Long memberId, TokenRequest request) {
        tokenRepository.save(Token.create(memberId, request));
    }
}
