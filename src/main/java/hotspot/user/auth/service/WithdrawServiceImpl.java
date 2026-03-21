package hotspot.user.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.auth.controller.port.WithdrawService;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.service.port.TokenRepository;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.Member;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.member.service.port.SocialAccountRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawServiceImpl implements WithdrawService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TokenRepository tokenRepository;
    private final JwtProvider jwtProvider;

    @Override
    public void withdraw(Long memberId, TokenRequest request) {
        log.info("회원 탈퇴 프로세스 시작: memberId={}", memberId);

        // 1. 회원 존재 여부 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        String refreshToken = request.refreshToken();

        // 2. 토큰 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }

        // 3. 토큰에서 사용자 정보 추출 및 소유권 검증
        PrincipalDetails principal = (PrincipalDetails) jwtProvider.getAuthenticationFromRefreshToken(refreshToken)
                .getPrincipal();

        if (!memberId.equals(principal.getId())) {
            log.warn("탈퇴 시도 보안 경고: 요청자 memberId={}와 토큰 소유자 memberId={} 불일치", memberId, principal.getId());
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }

        // 4. 토큰 데이터 삭제
        tokenRepository.deleteByMemberId(memberId);

        // 5. 회선 정보에서 memberId 해제
        subscriptionRepository.findByMemberId(memberId).ifPresent(subscription -> {
            Subscription updatedSubscription = subscription.updateMember(null);
            subscriptionRepository.save(updatedSubscription);
        });

        // 6. 소셜 계정 정보 삭제
        socialAccountRepository.deleteByMemberId(memberId);

        // 7. 회원 정보 최종 삭제
        memberRepository.delete(member);

        log.info("회원 탈퇴 프로세스 완료: memberId={}", memberId);
    }
}
