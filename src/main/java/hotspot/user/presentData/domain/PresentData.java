package hotspot.user.presentData.domain;

import java.time.LocalDateTime;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.PresentDataErrorCode;
import hotspot.user.subscription.domain.Subscription;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PresentData {

    private final Long presentDataId;
    private final Subscription targetSubscription;
    private final Subscription provideSubscription;
    private final Long dataAmount;
    private final LocalDateTime createdTime;

    @Builder
    public PresentData(Long presentDataId,
                       Subscription targetSubscription,
                       Subscription provideSubscription,
                       Long dataAmount, LocalDateTime createdTime) {

        // 생성 시점에 비즈니스 로직 검증
        validateNotSelfGift(provideSubscription, targetSubscription);

        this.presentDataId = presentDataId;
        this.targetSubscription = targetSubscription;
        this.provideSubscription = provideSubscription;
        this.dataAmount = dataAmount;
        this.createdTime = createdTime;
    }

    // 본인에게 선물하는지 검증
    private void validateNotSelfGift(Subscription provider, Subscription target) {
        if (provider != null && target != null && provider.getId().equals(target.getId())) {
            throw new ApplicationException(PresentDataErrorCode.PRESENT_DATA_SELF_GIFT);
        }
    }
}
