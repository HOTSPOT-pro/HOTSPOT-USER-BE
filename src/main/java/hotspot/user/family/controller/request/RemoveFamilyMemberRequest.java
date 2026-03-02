package hotspot.user.family.controller.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import lombok.Builder;

/**
 * 가족 구성원 삭제 신청 request dto
 * @param targetSubIdList
 */
@Builder
public record RemoveFamilyMemberRequest(
        @NotEmpty(message = "삭제 대상 회선 ID 목록은 비어 있을 수 없습니다.")
        List<Long> targetSubIdList
) {
}
