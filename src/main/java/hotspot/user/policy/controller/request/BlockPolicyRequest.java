package hotspot.user.policy.controller.request;

import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 정책 생성 / 수정 request dto
 */
public record BlockPolicyRequest(
        @NotBlank(message = "정책 이름은 필수입니다.")
        String name,
        @NotNull(message = "정책 타입은 필수입니다.")
        PolicyType policyType,
        @NotNull(message = "정책 스냅샷은 필수입니다.")
        PolicySnapshot policySnapshot,
        @NotBlank(message = "정책 설명은 필수입니다.")
        String policyDescription,
        boolean isActive
) {
}
