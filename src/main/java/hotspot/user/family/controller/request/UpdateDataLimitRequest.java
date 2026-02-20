package hotspot.user.family.controller.request;

/**
 * 구성원 데이터 한도 업데이트 request dto
 * @param familyId
 * @param subId
 * @param dataLimit
 */
public record UpdateDataLimitRequest(
        Long familyId,
        Long subId,
        int dataLimit
) {
}
