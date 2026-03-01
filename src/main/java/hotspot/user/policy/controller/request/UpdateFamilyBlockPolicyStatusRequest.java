package hotspot.user.policy.controller.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;



/** 가족 정책 상태 업데이트 (비/활성화)
 * @param familyId
 * @param blockPolicyIdList
 */
public record UpdateFamilyBlockPolicyStatusRequest(
        @NotNull Long familyId,
        @NotNull List<Long> blockPolicyIdList
) {
}
