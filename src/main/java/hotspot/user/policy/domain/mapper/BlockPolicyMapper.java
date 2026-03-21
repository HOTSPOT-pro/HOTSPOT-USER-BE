package hotspot.user.policy.domain.mapper;


import hotspot.user.policy.controller.request.BlockPolicyRequest;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;

/**
 * request -> 도메인
 * 도메인 -> response
 */
public class BlockPolicyMapper {

    // request -> domain
    public static BlockPolicy toBlockPolicy(BlockPolicyRequest request, Long familyId) {
        return BlockPolicy.builder()
                .name(request.name())
                .familyId(familyId)
                .policyType(request.policyType())
                .policySnapshot(request.policySnapshot())
                .policyDescription(request.policyDescription())
                .isActive(request.isActive())
                .build();
    }

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
