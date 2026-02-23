package hotspot.user.usage.reportUsage.infrastructure.keybuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class ReportUsageRedisKeyBuilder {

    private static final DateTimeFormatter yyyyMM =
            DateTimeFormatter.ofPattern("yyyyMM");

    private static final DateTimeFormatter yyyyMMdd =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private ReportUsageRedisKeyBuilder() {}

    public static String monthlyAppUsage(Long subId, LocalDate date) {
        return "usage:app:" + subId + ":" + date.format(yyyyMM);
    }

    public static String dailyAppUsage(Long subId, LocalDate date) {
        return "usage:app:" + subId + ":" + date.format(yyyyMMdd);
    }
}
