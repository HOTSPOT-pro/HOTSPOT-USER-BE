package hotspot.user.common.util.redis;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class RedisUsageCalculator {

    private static final double KB_TO_GB = 1024.0 * 1024.0;

    private RedisUsageCalculator() {}

    public static double kbToGb(double kb) {
        double safeKb = Math.max(kb, 0);
        double gb = safeKb / KB_TO_GB;

        return BigDecimal.valueOf(gb)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static double calculateRemain(double limitKb, double usedKb) {
        return Math.max(limitKb - usedKb, 0);
    }

    public static int calculatePercent(double usedKb, double limitKb) {
        if (limitKb <= 0) {
            return 0;
        }

        double safeUsed = Math.max(usedKb, 0);
        double percent = (safeUsed / limitKb) * 100;

        int rounded = BigDecimal.valueOf(percent)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        return Math.min(Math.max(rounded, 0), 100); // <- 초과표시 원하면 제거
    }
}
