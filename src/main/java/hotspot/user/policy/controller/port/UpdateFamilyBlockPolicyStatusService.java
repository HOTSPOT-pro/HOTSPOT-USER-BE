package hotspot.user.policy.controller.port;

import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.request.UpdateFamilyBlockPolicyStatusRequest;
import hotspot.user.policy.controller.request.UpdatePolicySubRequest;
import hotspot.user.policy.controller.response.UpdateFamilyBlockPolicyStatusResponse;
import hotspot.user.policy.controller.response.UpdatePolicySubResponse;

/**
 * 가족 정책 상태 업데이트 서비스 (비/활성화)
 */
public interface UpdateFamilyBlockPolicyStatusService {
    UpdateFamilyBlockPolicyStatusResponse updateFamilyBlockPolicyStatus(
            UpdateFamilyBlockPolicyStatusRequest request,
            Long memberId,
            Long requesterFamilyId);
}
