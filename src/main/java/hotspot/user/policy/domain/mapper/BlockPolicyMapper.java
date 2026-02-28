package hotspot.user.policy.domain.mapper;

import java.util.List;

import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.controller.response.UpdatePolicySubResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySub;

/**
 * request -> 도메인
 * 도메인 -> response
 */
public class BlockPolicyMapper {

    // request -> domain

    // domain -> response
    // 관리자 차단 정책 도메인 -> response dto로 매핑
    public static BlockPolicyResponse toBlockPolicyResponse(BlockPolicy blockPolicy) {
        return BlockPolicyResponse.builder()
                .id(blockPolicy.getId())
                .name(blockPolicy.getName())
                .familyId(blockPolicy.getFamilyId())
                .policyType(blockPolicy.getPolicyType())
                .policySnapshot(blockPolicy.getPolicySnapshot())
                .policyDescription(blockPolicy.getPolicyDescription())
                .isActive(blockPolicy.isActive())
                .build();
    }
}
