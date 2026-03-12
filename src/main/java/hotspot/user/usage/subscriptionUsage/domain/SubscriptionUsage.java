package hotspot.user.usage.subscriptionUsage.domain;

import hotspot.user.common.util.UsageCalculator;

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
        return UsageCalculator.calculateRemain(planLimitKb, planUsedKb);
    }

    public int remainPercent() {
        return UsageCalculator.calculatePercent(remainKb(), planLimitKb);
    }

    public double limitGb() {
        return UsageCalculator.kbToGb(planLimitKb);
    }

    public double usedGb() {
        return UsageCalculator.kbToGb(planUsedKb);
    }

    public double remainGb() {
        return UsageCalculator.kbToGb(remainKb());
    }
}
