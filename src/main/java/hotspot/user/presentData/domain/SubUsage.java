package hotspot.user.presentData.domain;

import hotspot.user.common.util.redis.RedisUsageCalculator;

public class SubUsage {

    private final double usedKb;
    private final double limitKb;

    public SubUsage(double usedKb, double limitKb) {
        this.usedKb = usedKb;
        this.limitKb = limitKb;
    }

    public double usedGb() {
        return RedisUsageCalculator.kbToGb(usedKb);
    }

    public double limitGb() {
        return RedisUsageCalculator.kbToGb(limitKb);
    }

    public double remainGb() {
        double remainKb =
                RedisUsageCalculator.calculateRemain(limitKb, usedKb);
        return RedisUsageCalculator.kbToGb(remainKb);
    }

    public int percent() {
        return RedisUsageCalculator.calculatePercent(usedKb, limitKb);
    }
}
