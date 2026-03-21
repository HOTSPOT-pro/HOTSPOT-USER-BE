package hotspot.user.policy.controller.port;

import java.util.List;

import hotspot.user.policy.controller.response.BlockPolicyResponse;

/**
 * 우리 가족이 생성한 정책 조회
 */
public interface FindFamilyBlockPolicyService {
    List<BlockPolicyResponse> findAllByFamilyId(Long memberId, Long familyId);
}
