package hotspot.user.auth.controller.response;

import hotspot.user.member.domain.FamilyRole;
import lombok.Builder;

/**
 * 온보딩 결과 dto
 * @param subId
 * @param familyId
 * @param name
 * @param email
 * @param phone
 * @param familyRole
 * @param tokenResponse
 */
@Builder
public record OnboardingResponse(
        Long subId,
        Long familyId,
        String name,
        String email,
        String phone,
        FamilyRole familyRole,
        TokenResponse tokenResponse
) {
}
