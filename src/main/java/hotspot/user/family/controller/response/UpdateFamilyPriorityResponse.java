package hotspot.user.family.controller.response;

import java.util.List;

import hotspot.user.family.domain.PriorityType;
import lombok.Builder;

/**
 * 가족 우선순위 정책 업데이트 response dto
 * @param familyId 가족 ID
 * @param priorityType 변경된 우선순위 타입
 * @param memberPriorities 최종 적용된 구성원별 우선순위 리스트
 */
@Builder
public record UpdateFamilyPriorityResponse(
        Long familyId,
        PriorityType priorityType,
        List<MemberPriorityResponse> memberPriorities
) {

}
