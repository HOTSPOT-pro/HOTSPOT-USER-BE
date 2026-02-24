package hotspot.user.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.auth.controller.port.WithdrawService;
import hotspot.user.auth.service.port.TokenRepository;
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

    @Override
    public void withdraw(Long memberId) {
        log.info("회원 탈퇴 프로세스 시작: memberId={}", memberId);

        // 1. 토큰 데이터 삭제 (Redis 또는 DB의 Refresh Token 무효화)
        tokenRepository.deleteByMemberId(memberId);

        // 2. 회선 정보에서 memberId 해제 (Subscription 업데이트)
        subscriptionRepository.findByMemberId(memberId).ifPresent(subscription -> {
            Subscription updatedSubscription = subscription.updateMember(null);
            subscriptionRepository.save(updatedSubscription);
            log.debug("회선 연결 해제 완료: subId={}", subscription.getId());
        });

        // 3. 소셜 계정 정보 삭제 (SocialAccount)
        socialAccountRepository.findByMemberId(memberId).ifPresent(socialAccount -> {
            socialAccountRepository.deleteByMemberId(socialAccount.getMemberId());
            log.debug("소셜 계정 정보 삭제 완료: memberId={}", memberId);
        });

        // 4. 회원 정보 최종 삭제 (Member)
        memberRepository.findById(memberId).ifPresent(member -> {
            memberRepository.deleteById(member.getId());
            log.info("회원 데이터 최종 삭제 완료: memberId={}", memberId);
        });

        log.info("회원 탈퇴 프로세스 종료: memberId={}", memberId);
    }
}
