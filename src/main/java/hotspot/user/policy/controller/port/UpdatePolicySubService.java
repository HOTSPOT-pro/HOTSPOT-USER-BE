package hotspot.user.policy.controller.port;

import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.request.UpdatePolicySubRequest;
import hotspot.user.policy.controller.response.UpdatePolicySubResponse;

/**
 * 구성원별 정책 업데이트 서비스 코드
 */
public interface UpdatePolicySubService {
    UpdatePolicySubResponse updatePolicySub(
            UpdatePolicySubRequest request,
            Long requesterMemberId,
            FamilyRole requesterRole);
}
