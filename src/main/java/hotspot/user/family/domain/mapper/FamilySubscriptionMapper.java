package hotspot.user.family.domain.mapper;

import hotspot.user.family.controller.request.UpdateFamilyRoleRequest;
import hotspot.user.family.controller.response.UpdateDataLimitResponse;
import hotspot.user.family.controller.response.UpdateFamilyRoleResponse;
import hotspot.user.family.domain.FamilySubscription;

/**
 * request Dto -> 도메인
 * 도메인 -> response Dto
 */

public class FamilySubscriptionMapper {
    // request -> domain


    // domain -> response
    public static UpdateDataLimitResponse toUpdateDataLimitResponse(FamilySubscription familySub) {
        return UpdateDataLimitResponse.builder()
                .familyId(familySub.getFamily().getId())
                .subId(familySub.getSubscription().getId())
                .dataLimit(familySub.getDataLimit())
                .build();
    }

    public static UpdateFamilyRoleResponse toUpdateFamilyRoleResponse(FamilySubscription familySub) {
        return UpdateFamilyRoleResponse.builder()
                .familyId(familySub.getFamily().getId())
                .subId(familySub.getSubscription().getId())
                .familyRole(familySub.getFamilyRole())
                .build();
    }
}
