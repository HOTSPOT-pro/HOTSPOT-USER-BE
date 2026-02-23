package hotspot.user.member.controller.response;

import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.Status;

/**
 * 회원 정보 응답 DTO
 */
public record MemberResponse(
        Long id,
        String name,
        String email,
        String phone,
        FamilyRole familyRole,
        Long familyId,
        Long subId,
        Status status
) {
}
