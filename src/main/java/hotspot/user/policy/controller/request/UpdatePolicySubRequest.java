package hotspot.user.policy.controller.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/**
 * 구성원별 정책 업데이트 request dto
 * @param familyId
 * @param subId
 * @param blockPolicyIdList
 */
public record UpdatePolicySubRequest(
        @NotNull Long familyId,
        @NotNull Long subId,
        @NotNull List<Long> blockPolicyIdList
) {
}
