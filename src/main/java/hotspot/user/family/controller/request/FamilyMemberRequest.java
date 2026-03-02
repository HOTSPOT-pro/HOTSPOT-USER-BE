package hotspot.user.family.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

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
        @Pattern(regexp = "^01(?:0|1|[6-9])-?(?:\\d{3}|\\d{4})-?\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phone,

        @NotNull(message = "가족 내 역할은 필수입니다.")
        FamilyRole targetFamilyRole
) {
}
