package hotspot.user.presentData.domain.mapper;

import java.time.LocalDateTime;

import hotspot.user.common.util.UsageCalculator;
import hotspot.user.presentData.controller.response.SendPresentDataResponse;
import hotspot.user.presentData.domain.PresentData;
import hotspot.user.subscription.domain.Subscription;

/**
 * 데이터 선물하기 dto <-> domain
 */
public class SendPresentDataMapper {

    // requst -> domain
    public static PresentData toPresentData(
            Subscription provideSubscription,
            Subscription targetSubscription,
            Long dataAmount
    ) {
        return PresentData.builder()
                .provideSubscription(provideSubscription)
                .targetSubscription(targetSubscription)
                .dataAmount(dataAmount)
                .createdTime(LocalDateTime.now())
                .build();
    }

    // domain -> response
    public static SendPresentDataResponse toSendPresentDataResponse(PresentData presentData) {
        double dataAmount = UsageCalculator.kbToGb(presentData.getDataAmount());
        return SendPresentDataResponse.builder()
                .targetSubId(presentData.getTargetSubscription().getId())
                .provideSubId(presentData.getProvideSubscription().getId())
                .dataAmount((long) dataAmount)
                .createdTime(presentData.getCreatedTime())
                .build();
    }
}
