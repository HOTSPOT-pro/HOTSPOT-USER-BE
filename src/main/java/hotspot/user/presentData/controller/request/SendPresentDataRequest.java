package hotspot.user.presentData.controller.request;

/**
 * 데이터 선물하기 request dto
 * @param targetSubId
 * @param dataAmount
 */
public record SendPresentDataRequest(
        Long targetSubId,
        Long dataAmount
) {
}
