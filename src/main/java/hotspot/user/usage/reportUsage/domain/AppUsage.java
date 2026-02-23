package hotspot.user.usage.reportUsage.domain;

import java.util.Objects;

public class AppUsage {

    private final Long appId;
    private final double usedGb;

    public AppUsage(Long appId, double usedGb) {
        this.appId = Objects.requireNonNull(appId);
        this.usedGb = Math.max(usedGb, 0);
    }

    public Long getAppId() {
        return appId;
    }

    public double getUsedGb() {
        return usedGb;
    }
}
