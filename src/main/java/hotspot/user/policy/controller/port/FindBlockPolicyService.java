package hotspot.user.policy.controller.port;

import java.util.List;

import hotspot.user.policy.controller.response.BlockPolicyResponse;

/**
 * 관리자가 생성한 정책 조회
 */
public interface FindBlockPolicyService {
    List<BlockPolicyResponse> findAll();
}
