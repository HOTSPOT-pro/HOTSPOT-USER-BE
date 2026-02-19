package hotspot.user.policy.domain.mapper;

import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;

/**
 * request -> 도메인
 * 도메인 -> response
 */
public class BlockPolicyMapper {

    // request -> domain

    // domain -> response
    public static BlockPolicyResponse toBlockPolicyResponse(BlockPolicy blockPolicy) {
        return BlockPolicyResponse.from(blockPolicy);
    }
}
