package hotspot.user.family.controller.response;

import hotspot.user.member.controller.response.MemberResponse;

import java.util.List;

/**
 * 가족 정보 조회 응답 Dto
 */
public record FamilyInfoResponse(
        Long familyId,
        int familyNum,
        int familyDataAmount,
        List<MemberResponse> memberInfoList
) {
}
