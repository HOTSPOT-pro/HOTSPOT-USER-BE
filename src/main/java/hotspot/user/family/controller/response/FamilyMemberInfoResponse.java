package hotspot.user.family.controller.response;

import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;
import lombok.Builder;

/**
 * 가족 정보 조회 시 구성원 세부 정보
 * @param id
 * @param name
 * @param email
 * @param phone
 * @param familyRole
 * @param familyId
 * @param subId
 * @param status
 */
@Builder
public record FamilyMemberInfoResponse(
        Long id,
        String name,
        String phone,
        FamilyRole familyRole,
        Long familyId,
        Long subId,
        Status status
) {
}
