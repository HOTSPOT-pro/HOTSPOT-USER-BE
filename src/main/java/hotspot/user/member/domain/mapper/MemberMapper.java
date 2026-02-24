package hotspot.user.member.domain.mapper;

import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.controller.response.MemberResponse;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.domain.Status;

/**
 * Member 도메인과 DTO 간의 변환을 담당하는 매퍼
 */
public class MemberMapper {

    // Request -> Domain (소셜 회원가입 시)
    public static Member toMember(CreateSocialAccountRequest request) {
        return Member.builder()
                .name(request.name())
                .status(Status.PENDING)
                .build();
    }

    // Domain -> Response
    public static MemberResponse toMemberResponse(MemberDetailInfo info, String decryptedPhone) {
        return MemberResponse.builder()
             .id(info.getMember().getId())
             .name(info.getMember().getName())
             .email(info.getEmail())
             .phone(decryptedPhone)
             .familyRole(info.getRole())
             .familyId(info.getFamilyId())
             .subId(info.getSubId())
             .status(info.getMember().getStatus())
             .build();
    }

}
