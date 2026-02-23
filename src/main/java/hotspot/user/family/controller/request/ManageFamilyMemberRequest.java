package hotspot.user.family.controller.request;

import hotspot.user.family.domain.ApplyType;
import hotspot.user.member.domain.FamilyRole;

/**
 * 가족 구성원 추가 / 삭제 신청 request dto
 * @param targetSubId
 * @param applyType
 * @param docUrl
 */
public record ManageFamilyMemberRequest(
        Long targetSubId,
        ApplyType applyType,
        FamilyRole targetFamilyRole,
        String docUrl
) {
}
