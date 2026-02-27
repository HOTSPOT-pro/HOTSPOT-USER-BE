package hotspot.user.family.controller.request;

import jakarta.validation.constraints.Min;

import hotspot.user.common.constant.FamilyConstant;

/**
 * 구성원 데이터 한도 업데이트 request dto
 * @param familyId
 * @param subId
 * @param dataLimit
 * @param isLocked
 */
public record UpdateDataLimitRequest(
        Long familyId,
        Long subId,
        @Min(value = FamilyConstant.UNLIMITED_DATA_LIMIT, message = "데이터 한도는 -1(무한대) 이상이어야 합니다.")
        long dataLimit,
        boolean isLocked // 데이터 차단 여부
) {
}
