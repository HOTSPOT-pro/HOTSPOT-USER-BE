package hotspot.user.family.controller.response;

import hotspot.user.common.util.UsageCalculator;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.member.domain.FamilyRole;

/**
 * 가족 구성원(회선) 정보 응답 DTO
 */
public record FamilySubscriptionResponse(
        Long id,
        Long subscriptionId,
        Long familyId,
        FamilyRole role,
        int priority,
        double dataLimit
) {
    public static FamilySubscriptionResponse from(FamilySubscription familySubscription) {
        return new FamilySubscriptionResponse(
                familySubscription.getId(),
                familySubscription.getSubscription().getId(),
                familySubscription.getFamily().getId(),
                familySubscription.getFamilyRole(),
                familySubscription.getPriority(),
                UsageCalculator.kbToGb(familySubscription.getDataLimit())
        );
    }
}
