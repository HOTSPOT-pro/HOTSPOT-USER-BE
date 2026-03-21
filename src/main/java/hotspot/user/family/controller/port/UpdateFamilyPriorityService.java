package hotspot.user.family.controller.port;

import hotspot.user.family.controller.request.UpdateFamilyPriorityRequest;
import hotspot.user.family.controller.response.UpdateFamilyPriorityResponse;
import hotspot.user.member.domain.FamilyRole;

/**
 * 가족 우선순위 정책 업데이트 서비스 인터페이스
 */
public interface UpdateFamilyPriorityService {
    UpdateFamilyPriorityResponse updateFamilyPriority(
            UpdateFamilyPriorityRequest request,
            Long requesterFamilyId,
            FamilyRole requesterRole);
}
