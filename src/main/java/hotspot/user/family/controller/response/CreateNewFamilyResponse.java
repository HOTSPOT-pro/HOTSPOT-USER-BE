package hotspot.user.family.controller.response;

import hotspot.user.family.domain.ApplyType;
import lombok.Builder;

import java.util.List;

/**
 * 가족 신규 생성 response dto
 * @param familyId
 * @param applyType
 * @param familyMemberList
 */
@Builder
public record CreateNewFamilyResponse(
        Long familyId,
        ApplyType applyType,
        List<FamilyMemberResponse> familyMemberList
) {
}
