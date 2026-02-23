package hotspot.user.family.controller.response;

import java.util.List;

import hotspot.user.member.controller.response.MemberResponse;
import lombok.Builder;

/**
 * 가족 정보 조회 응답 Dto
 */
@Builder
public record FamilyInfoResponse(
        Long familyId,
        int familyNum,
        List<MemberResponse> memberInfoList
) {
}
