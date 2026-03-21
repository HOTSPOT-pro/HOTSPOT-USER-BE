package hotspot.user.usage.giftUsage.domain;

import hotspot.user.common.util.UsageCalculator;

public class GiftUsage {

    private final Long giftId;
    private final double limitKb;
    private final double usedKb;

    public GiftUsage(Long giftId, double limitKb, double usedKb) {
        this.giftId = giftId;
        this.limitKb = limitKb;
        this.usedKb = usedKb;
    }

    public Long giftId() {
        return giftId;
    }

    public double limitKb() { return limitKb; }

    public double usedKb() { return usedKb; }

    public double remainKb() {
        return UsageCalculator.calculateRemain(limitKb, usedKb);
    }

    public int remainPercent() {
        return UsageCalculator.calculatePercent(remainKb(), limitKb);
    }

    public double limitGb() {
        return UsageCalculator.kbToGb(limitKb);
    }

    public double usedGb() {
        return UsageCalculator.kbToGb(usedKb);
    }

    public double remainGb() {
        return UsageCalculator.kbToGb(remainKb());
    }
}
