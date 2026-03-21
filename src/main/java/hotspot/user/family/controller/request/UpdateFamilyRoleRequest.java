package hotspot.user.family.controller.request;

import hotspot.user.member.domain.FamilyRole;

public record UpdateFamilyRoleRequest(
        FamilyRole familyRole
) {
}
