package hotspot.user.auth.domain.mapper;

import hotspot.user.auth.controller.response.MemberInfoResponse;
import hotspot.user.member.domain.FamilyRole;

/**
 * MemberInfo request, response dto 매핑
 */
public class MemberInfoMapper {

    // domain -> response
    public static MemberInfoResponse toMemberInfoResponse(
            Long subId,
            Long familyId,
            String name,
            String email,
            String decryptedPhone,
            FamilyRole familyRole) {
        return MemberInfoResponse.builder()
                .subId(subId)
                .familyId(familyId)
                .name(name)
                .email(email)
                .phone(decryptedPhone)
                .familyRole(familyRole)
                .build();
    }
}
