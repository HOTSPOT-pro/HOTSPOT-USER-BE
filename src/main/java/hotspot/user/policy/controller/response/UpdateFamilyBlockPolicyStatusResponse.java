package hotspot.user.policy.controller.response;

import lombok.Builder;

import java.util.List;

/**
 * 가족 정책 중 활성화된 정책 ID만 리턴하는 응답 dto
 * @param familyId
 * @param blockedPolicyIdList
 */
@Builder
public record UpdateFamilyBlockPolicyStatusResponse(
        Long familyId,
        List<Long> blockedPolicyIdList
) {
}
