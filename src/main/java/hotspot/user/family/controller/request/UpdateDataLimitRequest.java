package hotspot.user.family.controller.request;

import jakarta.validation.constraints.Min;

/**
 * 구성원 데이터 한도 업데이트 request dto
 * @param familyId
 * @param subId
 * @param dataLimit
 */
public record UpdateDataLimitRequest(
        Long familyId,
        Long subId,
        @Min(value = -1, message = "데이터 한도는 -1(무한대) 이상이어야 합니다.")
        int dataLimit
) {
}
