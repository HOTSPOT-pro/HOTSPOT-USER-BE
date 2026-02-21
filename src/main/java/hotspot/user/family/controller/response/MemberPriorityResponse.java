package hotspot.user.family.controller.response;

import jakarta.validation.constraints.NotNull;

/**
 * 개별 구성원 우선순위 response dto
 * @param subId 회선 ID
 * @param priority 우선순위 값
 */
public record MemberPriorityResponse(
        Long subId,
        int priority
) {
}
