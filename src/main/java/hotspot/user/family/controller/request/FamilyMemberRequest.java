package hotspot.user.family.controller.request;

import hotspot.user.member.domain.FamilyRole;
import lombok.Builder;

/**
 * 가족 생성 / 구성원 추가 / 삭제 신청 시 피신청자 정보 request dto
 * @param name
 * @param phone
 * @param targetFamilyRole
 */
public record FamilyMemberRequest(
        String name,
        String phone,
        FamilyRole targetFamilyRole
) {
}
