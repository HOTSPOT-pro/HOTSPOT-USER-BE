package hotspot.user.family.controller.request;

import jakarta.validation.constraints.NotNull;

/**
 * 개별 구성원 우선순위 request dto
 * @param subId 회선 ID
 * @param priority 우선순위 값
 */
public record MemberPriorityRequest(
        @NotNull(message = "회선 ID는 필수입니다.")
        Long subId,

        @NotNull(message = "우선순위는 필수입니다.")
        int priority
) {
}
