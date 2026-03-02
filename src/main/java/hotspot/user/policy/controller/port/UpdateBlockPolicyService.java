package hotspot.user.policy.controller.port;

import hotspot.user.policy.controller.request.BlockPolicyRequest;
import hotspot.user.policy.controller.response.BlockPolicyResponse;

/**
 * 정책 내용 업데이트
 */
public interface UpdateBlockPolicyService {
    BlockPolicyResponse update(BlockPolicyRequest request, Long blockPolicyId, Long memberId, Long familyId);
}
