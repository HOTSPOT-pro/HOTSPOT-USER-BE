package hotspot.user.family.controller.request;

import java.util.List;

import lombok.Builder;

/**
 * 가족 구성원 삭제 신청 request dto
 * @param targetSubIdList
 */
@Builder
public record RemoveFamilyMemberRequest(
        List<Long> targetSubIdList
) {
}
