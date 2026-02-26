package hotspot.user.presentData.controller.request;

import jakarta.validation.constraints.NotNull;

/**
 * 데이터 선물하기 request dto
 * @param targetSubId
 * @param dataAmount
 */
public record SendPresentDataRequest(
        @NotNull Long targetSubId,
        @NotNull Long dataAmount
) {
}
