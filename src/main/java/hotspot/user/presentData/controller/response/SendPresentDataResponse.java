package hotspot.user.presentData.controller.response;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 데이터 선물하기 request dto
 * @param targetSubId
 * @param provideSubId
 * @param dataAmount
 * @param createdTime
 */
@Builder
public record SendPresentDataResponse(
        Long targetSubId, // 선물 대상자
        Long provideSubId, // 선물한 사람
        Long dataAmount,
        LocalDateTime createdTime
) {
}
