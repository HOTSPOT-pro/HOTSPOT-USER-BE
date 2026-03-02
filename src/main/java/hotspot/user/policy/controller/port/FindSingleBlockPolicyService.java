package hotspot.user.policy.controller.port;

import hotspot.user.policy.controller.response.BlockPolicyResponse;

/**
 * 단일 정책 조회
 */
public interface FindSingleBlockPolicyService {
    BlockPolicyResponse find(Long blockPolicyId, Long memberId, Long familyId);
}
