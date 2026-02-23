package hotspot.user.policy.controller.port;

import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.request.UpdateBlockPolicyRequest;
import hotspot.user.policy.controller.response.UpdateBlockPolicyResponse;

/**
 * 구성원별 정책 업데이트 서비스 코드
 */
public interface UpdateBlockPolicyService {
    UpdateBlockPolicyResponse updateBlockPolicy(
            UpdateBlockPolicyRequest request,
            Long requesterFamilyId,
            FamilyRole requesterRole);
}
