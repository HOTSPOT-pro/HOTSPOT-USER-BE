package hotspot.user.family.controller.request;

import hotspot.user.family.domain.ApplyType;
import hotspot.user.member.domain.FamilyRole;
import lombok.Builder;

/**
 * 가족 구성원 추가 / 삭제 신청 request dto
 * @param targetSubId
 * @param applyType
 * @param targetFamilyRole
 * @param docUrl
 */
@Builder
public record CreateNewFamilyRequest(
        Long targetSubId,
        ApplyType applyType,
        FamilyRole targetFamilyRole,
        String docUrl
) {
}
