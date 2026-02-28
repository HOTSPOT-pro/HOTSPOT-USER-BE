package hotspot.user.policy.domain.mapper;

import java.util.List;

import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.controller.response.UpdateBlockPolicyResponse;
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

    // PolicySub(적용된 시간 정책) 도메인을 BlockPolicyResponse DTO로 변환
    public static BlockPolicyResponse toBlockPolicyResponse(PolicySub policySub) {
        return BlockPolicyResponse.builder()
                .id(policySub.getId())
                .name(policySub.getDateSnapshot().getPolicyName())
                .policyType(policySub.getDateSnapshot().getPolicyType())
                .policySnapshot(policySub.getDateSnapshot().getData())
                .build();
    }

    // 구성원별 정책 업데이트response dto로 변환
    public static UpdateBlockPolicyResponse toUpdateBlockPolicyResponse(
            Long familyId, Long subId, List<Long> blockedPolicyIdList) {
        return UpdateBlockPolicyResponse.builder()
                .familyId(familyId)
                .subId(subId)
                .blockedPolicyIdList(blockedPolicyIdList)
                .build();
    }
}
