package hotspot.user.policy.controller.response;

import lombok.Builder;

import java.util.List;

/**
 * 구성원별 정책 업데이트 response dto
 * @param familyId
 * @param subId
 * @param blockPolicyIdList
 */

@Builder
public record UpdateBlockPolicyResponse(
        Long familyId,
        Long subId,
        List<Long> blockPolicyIdList
) {
}
