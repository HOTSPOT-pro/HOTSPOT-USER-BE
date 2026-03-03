package hotspot.user.member.domain.mapper;

import hotspot.user.family.controller.response.FamilyMemberInfoResponse;
import hotspot.user.member.domain.MemberDetailInfo;

/**
 * FamilyMemberInfo 도메인과 DTO 간의 변환을 담당하는 매퍼
 */
public class FamilyMemberInfoMapper {


    // Domain -> Response
    public static FamilyMemberInfoResponse toFamilyMemberInfoResponse(MemberDetailInfo info, String decryptedPhone) {
        return FamilyMemberInfoResponse.builder()
             .id(info.getMember().getId())
             .name(info.getMember().getName())
             .phone(decryptedPhone)
             .familyRole(info.getRole())
             .familyId(info.getFamilyId())
             .subId(info.getSubId())
             .status(info.getMember().getStatus())
             .build();
    }

}
