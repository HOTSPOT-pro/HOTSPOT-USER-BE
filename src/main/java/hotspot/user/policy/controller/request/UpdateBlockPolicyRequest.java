package hotspot.user.policy.controller.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 구성원별 정책 업데이트 request dto
 * @param familyId
 * @param subId
 * @param blockPolicyIdList
 */
public record UpdateBlockPolicyRequest(
        @NotNull Long familyId,
        @NotNull Long subId,
        @NotNull List<Long> blockPolicyIdList
) {
}
