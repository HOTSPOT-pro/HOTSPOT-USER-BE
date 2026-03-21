package hotspot.user.usage.familyUsage.domain;

import hotspot.user.common.util.UsageCalculator;

public class FamilySubUsage {

    private final double subLimitKb;
    private final double memberFamilyUsedKb;

    public FamilySubUsage(
            double subLimitKb,
            double memberFamilyUsedKb
    ) {
        this.subLimitKb = subLimitKb;
        this.memberFamilyUsedKb = memberFamilyUsedKb;
    }

    public double remainKb() {
        return UsageCalculator.calculateRemain(subLimitKb, memberFamilyUsedKb);
    }

    public int remainPercent() {
        return UsageCalculator.calculatePercent(remainKb(), subLimitKb);
    }

    public double limitGb() {
        return UsageCalculator.kbToGb(subLimitKb);
    }

    public double familyUsedGb() {
        return UsageCalculator.kbToGb(memberFamilyUsedKb);
    }

    public double remainGb() {
        return UsageCalculator.kbToGb(remainKb());
    }

    public static FamilySubUsage zero() {
        return new FamilySubUsage(0, 0);
    }
}
