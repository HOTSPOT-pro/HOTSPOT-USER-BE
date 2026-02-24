package hotspot.user.family.controller.response;

import hotspot.user.member.domain.FamilyRole;
import lombok.Builder;

@Builder
public record UpdateFamilyRoleResponse(
        Long familyId,
        Long subId,
        FamilyRole familyRole
) {
}
