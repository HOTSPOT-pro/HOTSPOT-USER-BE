package hotspot.user.member.controller.response;

import hotspot.user.member.domain.FamilyRole;

import hotspot.user.member.domain.Status;
import lombok.Builder;


/**
 * 회원 정보 응답 DTO
 */
@Builder
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
