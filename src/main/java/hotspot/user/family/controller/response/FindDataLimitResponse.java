package hotspot.user.family.controller.response;

import lombok.Builder;

/**
 * 구성원 데이터 한도 조회 response dto
 * @param name
 * @param isLocked
 * @param dataLimit
 * @param familyDataAmount
 */
@Builder
public record FindDataLimitResponse(
        String name,
        boolean isLocked,
        double dataLimit,
        double familyDataAmount

) {
}
