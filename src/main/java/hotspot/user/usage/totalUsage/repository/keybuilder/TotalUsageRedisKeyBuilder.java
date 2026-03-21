package hotspot.user.usage.totalUsage.repository.keybuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class TotalUsageRedisKeyBuilder {

    private static final DateTimeFormatter yyyyMM =
            DateTimeFormatter.ofPattern("yyyyMM");

    private static final DateTimeFormatter yyyyMMdd =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private TotalUsageRedisKeyBuilder(){}

    public static String planLimit(Long subId) {
        return "limit:sub:" + subId;
    }

    public static String planUsageMonth(Long subId, LocalDate date) {
        return "usage:sub:" + subId + ":" + date.format(yyyyMM);
    }

    public static String planUsageDay(Long subId, LocalDate date) {
        return "usage:sub:" + subId + ":" + date.format(yyyyMMdd);
    }

    public static String familyLimit(Long familyId, Long subId) {
        return "limit:family_sub:" + familyId + ":" + subId;
    }

    public static String giftIndex(Long subId, LocalDate date) {
        return "idx:gift:" + subId + ":" + date.format(yyyyMM);
    }

    public static String giftLimit(Long subId, Long giftId, LocalDate date) {
        return "limit:gift:" + subId + ":" + giftId + ":" + date.format(yyyyMM);
    }

    public static String giftUsage(Long subId, Long giftId, LocalDate date) {
        return "usage:gift:" + subId + ":" + giftId + ":" + date.format(yyyyMM);
    }
}
