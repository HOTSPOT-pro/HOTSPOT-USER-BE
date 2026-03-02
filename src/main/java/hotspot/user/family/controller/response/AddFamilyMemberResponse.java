package hotspot.user.family.controller.response;

import hotspot.user.family.controller.request.FamilyMemberRequest;
import hotspot.user.family.domain.ApplyType;
import lombok.Builder;

import java.util.List;

/**
 * 가족 구성원 추가 신청 결과 response dto
 * @param familyId
 * @param applyType
 * @param familyMemberList
 */
@Builder
public record AddFamilyMemberResponse(
        Long familyId,
        ApplyType applyType,
        List<FamilyMemberRequest> familyMemberList
) {
}
