package hotspot.user.member.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.auth.controller.response.LoginResponse;
import hotspot.user.auth.domain.mapper.LoginResponseMapper;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.controller.port.RegisterSocialMemberService;
import hotspot.user.member.controller.port.SocialLoginService;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.member.service.port.SocialAccountRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

/**
 * SocialLoginService 구현체
 * 회원 조회 및 가입 흐름 제어
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SocialLoginServiceImpl implements SocialLoginService {

    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;
    private final RegisterSocialMemberService registerSocialMemberService;
    private final SubscriptionRepository subscriptionRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;

    @Override
    public LoginResponse login(CreateSocialAccountRequest request) {
        // 1. 이메일로 소셜 계정 조회
        Optional<SocialAccount> socialAccountOptional = socialAccountRepository.findByEmail(request.email());

        if (socialAccountOptional.isPresent()) {
            // 2. 소셜 계정이 있으면 연결된 회원 정보 반환 (기존 회원)
            SocialAccount socialAccount = socialAccountOptional.get();
            Member member = memberRepository.findById(socialAccount.getMemberId())
                    .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

            // 3. FamilyRole 및 FamilyId 조회 (기존 회원)
            FamilyRole familyRole = FamilyRole.CHILD; // 기본값
            Long familyId = null;
            Optional<Subscription> subscriptionOptional =
                    subscriptionRepository.findByMemberId(member.getId());

            if (subscriptionOptional.isPresent()) {
                Optional<FamilySubscription> familySubscriptionOptional =
                        familySubscriptionRepository.findBySubId(subscriptionOptional.get().getId());
                if (familySubscriptionOptional.isPresent()) {
                    familyRole = familySubscriptionOptional.get().getFamilyRole();
                    familyId = familySubscriptionOptional.get().getFamily().getId();
                }
            }

            return LoginResponseMapper.from(member, socialAccount, familyRole, familyId);
        }

        // 3. 소셜 계정이 없으면 신규 회원 가입 진행
        return registerSocialMemberService.register(request);
    }
}
