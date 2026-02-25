package hotspot.user.auth.domain.mapper;

import hotspot.user.auth.controller.response.OnboardingResponse;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;

/**
 * Onboarding request, response dto 매핑
 */
public class OnboardingMapper {

    // domain -> response
    public static OnboardingResponse toOnboardingResponse(
            Member member,
            FamilySubscription familySub,
            SocialAccount socialAccount,
            String decryptedPhone,
            TokenResponse tokenResponse) {
        return OnboardingResponse.builder()
                .subId(familySub.getSubscription().getId())
                .familyId(familySub.getFamily().getId())
                .name(member.getName())
                .email(socialAccount.getEmail())
                .phone(decryptedPhone)
                .familyRole(familySub.getFamilyRole())
                .tokenResponse(tokenResponse)
                .build();
    }
}
