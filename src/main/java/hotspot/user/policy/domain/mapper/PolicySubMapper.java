package hotspot.user.policy.domain.mapper;

import java.util.List;

import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.controller.response.UpdatePolicySubResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySub;

/**
 * 정책-회선 매핑(PolicySub) 관련 변환 매퍼
 */
public class PolicySubMapper {

    // PolicySub(적용된 시간 정책) 도메인을 BlockPolicyResponse DTO로 변환
    public static BlockPolicyResponse toBlockPolicyResponse(PolicySub policySub, BlockPolicy blockPolicy) {
        if (blockPolicy == null) {
            return null;
        }

        return BlockPolicyResponse.builder()
                .id(policySub.getId())
                .name(blockPolicy.getName())
                .familyId(blockPolicy.getFamilyId())
                .policyType(blockPolicy.getPolicyType())
                .policySnapshot(blockPolicy.getPolicySnapshot())
                .policyDescription(blockPolicy.getPolicyDescription())
                .isActive(policySub.isActive())
                .build();
    }

    // 구성원별 정책 업데이트 response dto로 변환
    public static UpdatePolicySubResponse toUpdatePolicySubResponse(
            Long familyId, Long subId, List<Long> blockedPolicyIdList) {
        return UpdatePolicySubResponse.builder()
                .familyId(familyId)
                .subId(subId)
                .blockedPolicyIdList(blockedPolicyIdList)
                .build();
    }
}
