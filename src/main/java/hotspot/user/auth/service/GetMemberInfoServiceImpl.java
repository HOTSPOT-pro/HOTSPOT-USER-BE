package hotspot.user.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.auth.controller.port.GetMemberInfoService;
import hotspot.user.auth.controller.response.MemberInfoResponse;
import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.member.service.port.SocialAccountRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 이후 내 정보 조회하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMemberInfoServiceImpl implements GetMemberInfoService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final PhoneDecryptor phoneDecryptor;

    @Override
    public MemberInfoResponse getMemberInfo(Long memberId, String email) {
        // 1. 기본 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 2. 소셜 계정 정보 조회 (이메일 확인용)
        SocialAccount socialAccount = socialAccountRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 3. 회선 정보 조회
        Subscription subscription = subscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 4. 가족 결합 정보 조회 (Role 및 familyId) => 이건 에러 말고 Null로 리턴 되어야함
        FamilySubscription familySub = familySubscriptionRepository.findBySubId(subscription.getId())
                .orElse(null);

        // 5. 전화번호 복호화
        String decryptedPhone = phoneDecryptor.decrypt(subscription.getPhoneEnc());

        // 6. 응답 조립 (요구하신 null 및 NONE 처리 반영)
        return MemberInfoResponse.builder()
                .subId(subscription.getId())
                .familyId(Optional.ofNullable(familySub).map(fs -> fs.getFamily().getId()).orElse(null)) // null 리턴
                .name(member.getName())
                .email(socialAccount.getEmail())
                .phone(decryptedPhone)
                .familyRole(Optional.ofNullable(familySub)
                        .map(FamilySubscription::getFamilyRole)
                        .orElse(FamilyRole.NONE)) // None 추가 (가족 아닌 상태)
                .build();
    }
}
