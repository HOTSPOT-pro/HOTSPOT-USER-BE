package hotspot.user.usage.subscriptionUsage.domain;

import hotspot.user.common.util.redis.RedisUsageCalculator;

public class SubscriptionUsage {

    private final Long subId;
    private final double planLimitKb;
    private final double planUsedKb;

    public SubscriptionUsage(
            Long subId,
            double planLimitKb,
            double planUsedKb
    ) {
        this.subId = subId;
        this.planLimitKb = planLimitKb;
        this.planUsedKb = planUsedKb;
    }

    public Long subId() {
        return subId;
    }

    public double remainKb() {
        return RedisUsageCalculator.calculateRemain(planLimitKb, planUsedKb);
    }

    public int remainPercent() {
        return RedisUsageCalculator.calculatePercent(remainKb(), planLimitKb);
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
}
