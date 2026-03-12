package hotspot.user.presentData.domain;

import hotspot.user.common.util.UsageCalculator;

public class SubUsage {

    private final double usedKb;
    private final double limitKb;

    public SubUsage(double usedKb, double limitKb) {
        this.usedKb = usedKb;
        this.limitKb = limitKb;
    }

    public double usedGb() {
        return UsageCalculator.kbToGb(usedKb);
    }

    public double limitGb() {
        return UsageCalculator.kbToGb(limitKb);
    }

    public double remainGb() {
        double remainKb =
                UsageCalculator.calculateRemain(limitKb, usedKb);
        return UsageCalculator.kbToGb(remainKb);
    }

    public int percent() {
        return UsageCalculator.calculatePercent(usedKb, limitKb);
    }
}
