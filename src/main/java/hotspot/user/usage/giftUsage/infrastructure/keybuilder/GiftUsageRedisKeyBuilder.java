package hotspot.user.usage.giftUsage.infrastructure.keybuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class GiftUsageRedisKeyBuilder {

    private static final DateTimeFormatter yyyyMM =
            DateTimeFormatter.ofPattern("yyyyMM");

    private GiftUsageRedisKeyBuilder() {}

    private static String formatYYYYMM(LocalDate date) {
        return date.format(yyyyMM);
    }

    public static String giftIndex(Long subId, LocalDate date) {
        return "idx:gift:" + subId + ":" + formatYYYYMM(date);
    }

    public static String giftLimit(Long subId, Long giftId, LocalDate date) {
        return "limit:gift:" + subId + ":" + giftId + ":" + formatYYYYMM(date);
    }

    public static String giftUsage(Long subId, Long giftId, LocalDate date) {
        return "usage:gift:" + subId + ":" + giftId + ":" + formatYYYYMM(date);
    }
}
