package hotspot.user.family.controller.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import hotspot.user.family.domain.ApplyType;
import lombok.Builder;

/**
 * 가족 신규 생성 dto
 * @param applyType
 * @param docUrl
 * @param familyMemberList
 */
@Builder
public record CreateNewFamilyRequest(
        @NotNull(message = "신청 타입은 필수입니다.")
        ApplyType applyType,

        @NotBlank(message = "증빙 서류 URL은 필수입니다.")
        String docUrl,

        @NotEmpty(message = "추가할 구성원 정보는 최소 1명 이상이어야 합니다.")
        @Size(max = 8, message = "한 번에 최대 8명까지만 추가 신청이 가능합니다.")
        @Valid
        List<FamilyMemberRequest> familyMemberList
) {
}
