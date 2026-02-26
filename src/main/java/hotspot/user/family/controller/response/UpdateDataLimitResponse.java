package hotspot.user.family.controller.response;

import lombok.Builder;

/**
 * 구성원 데이터 한도 업데이트 response dto
 * @param familyId
 * @param subId
 * @param dataLimit
 * @param isLocked
 */
@Builder
public record UpdateDataLimitResponse(
        Long familyId,
        Long subId,
        int dataLimit,
        boolean isLocked
) {
}
