package hotspot.user.usage.subscriptionUsage.domain;

import java.util.List;

import hotspot.user.common.util.redis.RedisUsageCalculator;

public class SubscriptionUsage {

    private final Long subId;
    private final double planLimitKb;
    private final double planUsedKb;
    private final List<GiftUsage> gifts;

    public SubscriptionUsage(
            Long subId,
            double planLimitKb,
            double planUsedKb,
            List<GiftUsage> gifts
    ) {
        this.subId = subId;
        this.planLimitKb = planLimitKb;
        this.planUsedKb = planUsedKb;
        this.gifts = gifts;
    }

    public Long subId() {
        return subId;
    }

    public List<GiftUsage> gifts() {
        return gifts;
    }

    public double remainKb() {
        return RedisUsageCalculator.calculateRemain(planLimitKb, planUsedKb);
    }

    public int usagePercent() {
        return RedisUsageCalculator.calculatePercent(planUsedKb, planLimitKb);
    }

    public double limitGb() {
        return RedisUsageCalculator.kbToGb(planLimitKb);
    }

    public double usedGb() {
        return RedisUsageCalculator.kbToGb(planUsedKb);
    }

    public double remainGb() {
        return RedisUsageCalculator.kbToGb(remainKb());
    }

    public double giftTotalLimitKb() {
        return gifts.stream()
                .mapToDouble(GiftUsage::limitKb)
                .sum();
    }

    public double giftTotalUsedKb() {
        return gifts.stream()
                .mapToDouble(GiftUsage::usedKb)
                .sum();
    }

    public double giftTotalRemainKb() {
        return RedisUsageCalculator.calculateRemain(
                giftTotalLimitKb(),
                giftTotalUsedKb()
        );
    }

    public int giftUsagePercent() {
        return RedisUsageCalculator.calculatePercent(
                giftTotalUsedKb(),
                giftTotalLimitKb()
        );
    }

    public double giftTotalLimitGb() {
        return RedisUsageCalculator.kbToGb(giftTotalLimitKb());
    }

    public double giftTotalUsedGb() {
        return RedisUsageCalculator.kbToGb(giftTotalUsedKb());
    }

    public double giftTotalRemainGb() {
        return RedisUsageCalculator.kbToGb(giftTotalRemainKb());
    }
}
