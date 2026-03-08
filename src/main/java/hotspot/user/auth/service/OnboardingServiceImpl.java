package hotspot.user.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.auth.controller.port.IssueTokenService;
import hotspot.user.auth.controller.port.OnboardingService;
import hotspot.user.auth.controller.request.OnboardingRequest;
import hotspot.user.auth.controller.response.OnboardingResponse;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.auth.domain.mapper.OnboardingMapper;
import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.common.crpyto.PhoneHashIndexer;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
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
    private final IssueTokenService issueTokenService;
    private final PhoneHashIndexer phoneHashIndexer;
    private final PhoneDecryptor phoneDecryptor;

    @Override
    public OnboardingResponse onboarding(Long memberId, String email, OnboardingRequest request) {
        // 1. 데이터 조회 및 회선 검증
        Subscription subscription = validateAndGetSubscription(request.phoneNumber());
        Member pendingMember = findPendingMember(memberId);
        SocialAccount socialAccount = findSocialAccount(pendingMember.getId(), email);

        // 2. 신규 승인 또는 기존 회원 통합 처리
        Member finalMember = handleMemberIntegration(subscription, pendingMember, socialAccount, request.birthDate());

        // 3. 가족 정보 조회 (Role 및 familyId)
        FamilySubscription familySub = getFamilySubscription(subscription.getId());

        // 4. 토큰 발급 (가족 ID 포함)
        TokenResponse tokenResponse = issueTokenService.issue(finalMember, email,
                Optional.ofNullable(familySub).map(FamilySubscription::getFamilyRole).orElse(FamilyRole.NONE),
                Optional.ofNullable(familySub).map(fs -> fs.getFamily().getId()).orElse(null));

        String decryptedPhone = phoneDecryptor.decrypt(subscription.getPhoneEnc());

        return OnboardingMapper.toOnboardingResponse(
                subscription.getId(),
                Optional.ofNullable(familySub).map(fs -> fs.getFamily().getId()).orElse(null),
                finalMember.getName(),
                socialAccount.getEmail(),
                decryptedPhone,
                Optional.ofNullable(familySub).map(FamilySubscription::getFamilyRole).orElse(FamilyRole.NONE),
                tokenResponse
        );
    }

    // 전화번호로 회선을 조회 및 검증
    private Subscription validateAndGetSubscription(String phoneNumber) {
        String phoneHash = phoneHashIndexer.toHash(phoneNumber);
        return subscriptionRepository.findByPhoneHash(phoneHash)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.SUBSCRIPTION_NOT_FOUND));
    }

    // 현재 온보딩 중인 PENDING 멤버를 조회
    private Member findPendingMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    // 멤버와 연결된 소셜 계정 정보를 조회
    private SocialAccount findSocialAccount(Long memberId, String email) {
        return socialAccountRepository.findByMemberIdAndEmail(memberId, email)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    // 회선의 상태에 따라 신규 회원 승인 또는 기존 회원 통합을 처리한다.
    private Member handleMemberIntegration(Subscription subscription, Member pendingMember,
                                           SocialAccount socialAccount, String birthDate) {
        // 이미 해당 회선에 APPROVED 회원이 있는 경우 (기존 회원 통합)
        if (subscription.getMember() != null && subscription.getMember().getStatus() == Status.APPROVED) {
            return mergeWithExistingMember(subscription.getMember(), pendingMember, socialAccount);
        }

        // 신규 회원 온보딩 승인
        return onboardNewMember(subscription, pendingMember, birthDate);
    }

    // 새로 생성된 소셜 계정을 기존 회원에게 연결하고 임시 멤버를 삭제
    private Member mergeWithExistingMember(Member existingMember, Member pendingMember, SocialAccount socialAccount) {
        log.info("기존 APPROVED 회원 발견: 기존 memberId={}, 신규 memberId={}",
                existingMember.getId(), pendingMember.getId());

        // 소셜 계정의 주인을 기존 회원으로 교체
        SocialAccount updatedAccount = socialAccount.updateMemberId(existingMember.getId());
        socialAccountRepository.save(updatedAccount);

        // 이번 로그인 시 생성되었던 임시 PENDING 멤버 삭제
        memberRepository.delete(pendingMember);

        return existingMember;
    }

    // PENDING 멤버를 APPROVED으로 전환하고 회선과 연결
    private Member onboardNewMember(Subscription subscription, Member pendingMember, String birthDate) {
        log.info("신규 회원 온보딩 진행: memberId={}", pendingMember.getId());

        // 멤버 정보 업데이트 및 승인
        Member approvedMember = pendingMember.onboard(birthDate);
        approvedMember = memberRepository.save(approvedMember);

        // 회선에 승인된 멤버 연결
        Subscription updatedSubscription = subscription.updateMember(approvedMember);
        subscriptionRepository.save(updatedSubscription);

        return approvedMember;
    }

    // 가족-회선 매핑 정보 조회
    private FamilySubscription getFamilySubscription(Long subId) {
        return familySubscriptionRepository.findBySubId(subId)
                .orElse(null);
    }
}
