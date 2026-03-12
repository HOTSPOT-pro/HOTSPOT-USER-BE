package hotspot.user.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class UsageCalculator {

    private static final double KB_TO_GB = 1024.0 * 1024.0;

    private UsageCalculator() {}

    public static double kbToGb(double kb) {


        if (kb < 0) {
            return -1;
        }

        double gb = kb / KB_TO_GB;

        return BigDecimal.valueOf(gb)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    // GB -> KB 로직 추가
    public static long gbToKb(double gb) {
        if (gb < 0) {
            return -1L;
        }
        return Math.round(gb * KB_TO_GB);
    }

    public static double calculateRemain(double limitKb, double usedKb) {
        return Math.max(limitKb - usedKb, 0);
    }

    public static int calculatePercent(double remainKb, double limitKb) {
        if (limitKb <= 0) {
            return 0;
        }

        double safeRemain = Math.max(remainKb, 0);
        double percent = (safeRemain / limitKb) * 100;

        int rounded = BigDecimal.valueOf(percent)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        return Math.min(Math.max(rounded, 0), 100); // <- 초과표시 원하면 제거
    }

    public static Long kbToGbCeil(long kb) {

        if (kb <= 0) {
            return 0L;
        }

        double gb = kb / (1024.0 * 1024.0);

        return (long) Math.ceil(gb);
    }
}
