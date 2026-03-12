package hotspot.user.usage.familyUsage.domain;

import java.util.Map;

import hotspot.user.common.util.UsageCalculator;

public class FamilyUsage {

    private final double familyLimitKb;
    private final double familyUsedKb;
    private final Map<Long, FamilySubUsage> subData;

    public FamilyUsage(
            double familyLimitKb,
            double familyUsedKb,
            Map<Long, FamilySubUsage> subData
    ) {
        this.familyLimitKb = familyLimitKb;
        this.familyUsedKb = familyUsedKb;
        this.subData = subData;
    }

    public double familyRemainKb() {
        return UsageCalculator.calculateRemain(familyLimitKb, familyUsedKb);
    }

    public int familyRemainPercent() {
        return UsageCalculator.calculatePercent(familyRemainKb(), familyLimitKb);
    }

    public double familyLimitGb() {
        return UsageCalculator.kbToGb(familyLimitKb);
    }

    public double familyUsedGb() {
        return UsageCalculator.kbToGb(familyUsedKb);
    }

    public double familyRemainGb() {
        return UsageCalculator.kbToGb(familyRemainKb());
    }

    public FamilySubUsage getSubOrZero(Long subId) {
        return subData.getOrDefault(subId, FamilySubUsage.zero());
    }
}
