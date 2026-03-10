package hotspot.user.usage.subscriptionUsage.infrastructure.keybuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class SubscriptionUsageRedisKeyBuilder {

    private static final DateTimeFormatter yyyyMM =
            DateTimeFormatter.ofPattern("yyyyMM");

    private static final DateTimeFormatter yyyyMMdd =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private SubscriptionUsageRedisKeyBuilder() {}

    private static String formatYYYYMM(LocalDate date) {
        return date.format(yyyyMM);
    }

    private static String formatYYYYMMDD(LocalDate date) {
        return date.format(yyyyMMdd);
    }

    public static String planLimit(Long subId) {
        return "limit:sub:" + subId;
    }

    public static String planUsageMonth(Long subId, LocalDate date) {
        return "usage:sub:" + subId + ":" + formatYYYYMM(date);
    }

    public static String planUsageDay(Long subId, LocalDate date) {
        return "usage:sub:" + subId + ":" + formatYYYYMMDD(date);
    }
}
