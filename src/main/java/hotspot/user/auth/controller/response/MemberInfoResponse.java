package hotspot.user.auth.controller.response;

import hotspot.user.member.domain.FamilyRole;
import lombok.Builder;

/**
 * 로그인 이후 정보 조회 dto
 * @param subId
 * @param familyId
 * @param name
 * @param email
 * @param phone
 * @param familyRole
 */
@Builder
public record MemberInfoResponse(
        Long subId,
        Long familyId,
        String name,
        String email,
        String phone,
        FamilyRole familyRole
) {
}
