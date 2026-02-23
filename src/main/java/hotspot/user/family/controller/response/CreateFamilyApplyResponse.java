package hotspot.user.family.controller.response;

import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.member.domain.FamilyRole;
import lombok.Builder;

/**
 * 가족 구성원 추가 / 삭제 신청 response dto
 * @param requesterSubId
 * @param targetSubId
 * @param familyId
 * @param applyType
 * @param targetFamilyRole
 * @param docUrl
 * @param status
 */
@Builder
public record CreateFamilyApplyResponse(
        Long requesterSubId,
        Long targetSubId,
        Long familyId,
        ApplyType applyType,
        FamilyRole targetFamilyRole,
        String docUrl,
        ApplyStatus status
) {
}
