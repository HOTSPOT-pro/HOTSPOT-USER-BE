package hotspot.user.family.controller.port;

import hotspot.user.family.controller.request.UpdateDataLimitRequest;
import hotspot.user.family.controller.response.UpdateDataLimitResponse;
import hotspot.user.member.domain.FamilyRole;

/**
 * 특정 구성원의 데이터 한도 업데이트
 */
public interface UpdateDataLimitService {
    UpdateDataLimitResponse updateDataLimit(UpdateDataLimitRequest request, Long requesterFamilyId, FamilyRole requesterRole);
}
