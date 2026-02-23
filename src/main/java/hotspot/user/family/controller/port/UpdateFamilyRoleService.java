package hotspot.user.family.controller.port;

import hotspot.user.family.controller.request.UpdateFamilyRoleRequest;
import hotspot.user.family.controller.response.UpdateFamilyRoleResponse;
import hotspot.user.member.domain.FamilyRole;

/**
 * Family Role 업데이트 서비스 인터페이스
 */
public interface UpdateFamilyRoleService {
    UpdateFamilyRoleResponse update(
            Long requesterFamilyId,
            FamilyRole requesterFamilyRole,
            Long targetFamilyId,
            Long targetSubId,
            UpdateFamilyRoleRequest request);
}
