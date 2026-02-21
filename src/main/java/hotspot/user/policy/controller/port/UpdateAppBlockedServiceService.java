package hotspot.user.policy.controller.port;

import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.request.UpdateAppBlockedServiceRequest;
import hotspot.user.policy.controller.response.UpdateAppBlockedServiceResponse;

/**
 * 구성원별 차단 앱 서비스 업데이트 서비스 코드
 */
public interface UpdateAppBlockedServiceService {
    UpdateAppBlockedServiceResponse updateAppBlockedService(
            UpdateAppBlockedServiceRequest request, 
            Long requesterFamilyId, 
            FamilyRole requesterRole);
}
