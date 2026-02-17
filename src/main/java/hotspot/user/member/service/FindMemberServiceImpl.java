package hotspot.user.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.controller.port.FindMemberService;
import hotspot.user.member.controller.response.MemberResponse;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.domain.mapper.MemberMapper;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.member.service.port.SocialAccountRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

/**
 * FindMemberService 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindMemberServiceImpl implements FindMemberService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;

    @Override
    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        // Optional 처리: 없으면 null
        SocialAccount socialAccount = socialAccountRepository.findByMemberId(id).orElse(null);
        Subscription subscription = subscriptionRepository.findByMemberId(id).orElse(null);

        FamilyRole familyRole = null;
        if (subscription != null) {
            familyRole = familySubscriptionRepository.findBySubId(subscription.getId())
                    .map(FamilySubscription::getFamilyRole)
                    .orElse(null);
        }

        return MemberMapper.toResponse(member, socialAccount, subscription, familyRole);
    }
}
