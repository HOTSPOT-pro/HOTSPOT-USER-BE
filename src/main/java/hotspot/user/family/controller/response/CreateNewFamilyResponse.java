package hotspot.user.family.controller.response;

import java.util.List;

import hotspot.user.family.domain.ApplyType;
import lombok.Builder;

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
