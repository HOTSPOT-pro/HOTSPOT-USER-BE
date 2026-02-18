package hotspot.user.auth.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.auth.controller.port.OnboardingService;
import hotspot.user.auth.controller.port.SaveTokenService;
import hotspot.user.auth.controller.request.OnboardingRequest;
import hotspot.user.auth.controller.request.TokenRequest;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.common.util.PhoneUtil;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.domain.Status;
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
public class OnboardingServiceImpl implements OnboardingService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final SaveTokenService saveTokenService;
    private final JwtProvider jwtProvider;

    @Override
    public TokenResponse onboarding(OnboardingRequest request) {
        // 1. 전화번호 해싱 및 Subscription 존재 확인
        String phoneHash = PhoneUtil.hashPhoneNumber(request.phoneNumber());
        Subscription subscription = subscriptionRepository.findByPhoneHash(phoneHash)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 2. 현재 PENDING 상태인 멤버 조회
        Member pendingMember = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 3. 기존에 해당 Subscription에 연결된 APPROVED 멤버가 있는지 확인
        Member finalMember;
        SocialAccount socialAccount = socialAccountRepository.findByMemberId(pendingMember.getId())
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (subscription.getMember() != null && subscription.getMember().getStatus() == Status.APPROVED) {
            log.info("기존 APPROVED 회원 발견: 기존 memberId={}, 신규 memberId={}",
                subscription.getMember().getId(), pendingMember.getId());

            finalMember = subscription.getMember();

            // SocialAccount를 기존 회원에게 연결
            socialAccount = socialAccount.updateMemberId(finalMember.getId());
            socialAccountRepository.save(socialAccount);

            // 임시로 생성되었던 PENDING 멤버 삭제
            memberRepository.delete(pendingMember);
        } else {
            log.info("신규 회원 온보딩 진행: memberId={}", pendingMember.getId());

            // PENDING 멤버를 APPROVED로 전환 및 정보 업데이트
            finalMember = pendingMember.onboard(request.birthDate());
            finalMember = memberRepository.save(finalMember);

            // Subscription에 멤버 연결
            subscription = subscription.updateMember(finalMember);
            subscriptionRepository.save(subscription);
        }

        // 4. FamilyRole 조회 (이미 가족 결합이 되어 있어야 함)
        FamilyRole familyRole = familySubscriptionRepository.findBySubId(subscription.getId())
                .map(FamilySubscription::getFamilyRole)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        // 5. 토큰 발급을 위한 Authentication 객체 생성
        PrincipalDetails principal = new PrincipalDetails(
            finalMember.getId(),
            socialAccount.getEmail(),
            familyRole,
            finalMember.getStatus()
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal, null, principal.getAuthorities()
        );

        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(authentication);

        // Redis에 토큰 저장
        saveTokenService.saveToken(finalMember.getId(), new TokenRequest(refreshToken));

        return new TokenResponse(accessToken, refreshToken);
    }
}
