package hotspot.user.policy.controller.port;

import hotspot.user.policy.controller.request.BlockPolicyRequest;
import hotspot.user.policy.controller.response.BlockPolicyResponse;

/**
 * 정책 생성
 */
public interface CreateBlockPolicyService {
    BlockPolicyResponse create(BlockPolicyRequest request, Long memberId, Long familyId);
}
