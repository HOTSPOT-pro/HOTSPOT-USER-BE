package hotspot.user.auth.domain.mapper;

import hotspot.user.auth.controller.response.OnboardingResponse;
import hotspot.user.auth.controller.response.TokenResponse;
import hotspot.user.member.domain.FamilyRole;

/**
 * Onboarding request, response dto 매핑
 */
public class OnboardingMapper {

    // domain -> response
    public static OnboardingResponse toOnboardingResponse(
            Long subId,
            Long familyId,
            String name,
            String email,
            String decryptedPhone,
            FamilyRole familyRole,
            TokenResponse tokenResponse) {
        return OnboardingResponse.builder()
                .subId(subId)
                .familyId(familyId)
                .name(name)
                .email(email)
                .phone(decryptedPhone)
                .familyRole(familyRole)
                .tokenResponse(tokenResponse)
                .build();
    }
}
