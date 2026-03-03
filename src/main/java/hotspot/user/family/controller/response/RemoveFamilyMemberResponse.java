package hotspot.user.family.controller.response;

import java.time.LocalDate;
import java.util.List;

import hotspot.user.family.domain.DeleteStatus;
import lombok.Builder;

/**
 * 가족 구성원 삭제 신청 결과 response dto
 * @param familyApplyId
 * @param familyId
 * @param subIdList
 * @param status
 * @param scheduleDate
 */
@Builder
public record RemoveFamilyMemberResponse(
        Long familyApplyId,
        Long familyId,
        List<Long> subIdList,
        DeleteStatus status,
        LocalDate scheduleDate
) {
}
