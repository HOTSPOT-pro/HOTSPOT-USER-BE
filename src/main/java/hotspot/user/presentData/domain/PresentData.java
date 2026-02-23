package hotspot.user.presentData.domain;

import java.time.LocalDateTime;

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
        this.presentDataId = presentDataId;
        this.targetSubscription = targetSubscription;
        this.provideSubscription = provideSubscription;
        this.dataAmount = dataAmount;
        this.createdTime = createdTime;
    }
}
