package hotspot.user.policy.controller.response;

import java.util.List;

import lombok.Builder;

/**
 * 구성원별 정책 업데이트 response dto
 * @param familyId
 * @param subId
 * @param blockedPolicyIdList
 */

@Builder
public record UpdatePolicySubResponse(
        Long familyId,
        Long subId,
        List<Long> blockedPolicyIdList
) {
}
