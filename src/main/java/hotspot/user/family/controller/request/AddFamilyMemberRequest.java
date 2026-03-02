package hotspot.user.family.controller.request;

import hotspot.user.family.domain.ApplyType;
import lombok.Builder;

import java.util.List;

/**
 * 가족 구성원 추가 신청 request dto
 * @param applyType
 * @param docUrl
 * @param familyMemberList
 */
public record AddFamilyMemberRequest(
        ApplyType applyType,
        String docUrl,
        List<FamilyMemberRequest> familyMemberList
) {
}
