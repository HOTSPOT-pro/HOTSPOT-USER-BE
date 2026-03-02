package hotspot.user.family.controller.response;

import hotspot.user.member.domain.FamilyRole;
import lombok.Builder;

/**
 * 가족 생성 / 구성원 추가 / 삭제 신청 시 피신청자 정보 response dto
 * @param name
 * @param phone
 * @param targetFamilyRole
 */
@Builder
public record FamilyMemberResponse(
        String name,
        String phone,
        FamilyRole targetFamilyRole
) {
}
