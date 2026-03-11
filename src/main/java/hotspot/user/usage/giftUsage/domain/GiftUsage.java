package hotspot.user.usage.giftUsage.domain;

import hotspot.user.common.util.redis.RedisUsageCalculator;

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
        return RedisUsageCalculator.calculateRemain(limitKb, usedKb);
    }

    public int remainPercent() {
        return RedisUsageCalculator.calculatePercent(remainKb(), limitKb);
    }

    public double limitGb() {
        return RedisUsageCalculator.kbToGb(limitKb);
    }

    public double usedGb() {
        return RedisUsageCalculator.kbToGb(usedKb);
    }

    public double remainGb() {
        return RedisUsageCalculator.kbToGb(remainKb());
    }
}
