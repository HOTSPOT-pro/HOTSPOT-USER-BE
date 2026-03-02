package hotspot.user.family.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import hotspot.user.member.domain.FamilyRole;

/**
 * 가족 생성 / 구성원 추가 / 삭제 신청 시 피신청자 정보 request dto
 * @param name
 * @param phone
 * @param targetFamilyRole
 */
public record FamilyMemberRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phone,

        @NotNull(message = "가족 내 역할은 필수입니다.")
        FamilyRole targetFamilyRole
) {
}
