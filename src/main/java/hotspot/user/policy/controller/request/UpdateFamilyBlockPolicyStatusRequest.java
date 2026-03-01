package hotspot.user.policy.controller.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;



/** 가족 정책 상태 업데이트 (비/활성화)
 * @param familyId
 * @param blockPolicyIdList
 */
public record UpdateFamilyBlockPolicyStatusRequest(
        @NotNull Long familyId,
        @NotNull List<Long> blockPolicyIdList
) {
}
