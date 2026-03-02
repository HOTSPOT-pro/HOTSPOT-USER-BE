package hotspot.user.family.controller.response;

import hotspot.user.family.domain.DeleteStatus;
import lombok.Builder;

/**
 * 가족 구성원 삭제 신청 결과 response dto
 * @param familyApplyId
 * @param familyId
 * @param subId
 * @param deleteStatus
 */
@Builder
public record RemoveFamilyMemberResponse(
        Long familyApplyId,
        Long familyId,
        Long subId,
        DeleteStatus deleteStatus
) {
}
