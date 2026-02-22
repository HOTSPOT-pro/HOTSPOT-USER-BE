package hotspot.user.family.controller.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import hotspot.user.family.domain.PriorityType;

/**
 * 가족 우선순위 정책 업데이트 request DTO
 * @param familyId
 * @param priorityType
 * @param memberPriorities
 */
public record UpdateFamilyPriorityRequest(
        @NotNull(message = "가족 ID는 필수입니다.")
        Long familyId,

        @NotNull(message = "우선순위 타입은 필수입니다.")
        PriorityType priorityType,

        // priority = FIFO인 경우는 Null값
        @Valid
        List<MemberPriorityRequest> memberPriorities
) {
}
