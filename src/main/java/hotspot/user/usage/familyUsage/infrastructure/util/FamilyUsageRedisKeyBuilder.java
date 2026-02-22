package hotspot.user.usage.familyUsage.infrastructure.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FamilyUsageRedisKeyBuilder {

    private static final DateTimeFormatter yyyyMM =
            DateTimeFormatter.ofPattern("yyyyMM");

    private static String currentYYYYMM() {
        return LocalDate.now().format(yyyyMM);
    }

    public static String familyLimit(Long familyId) {
        return "limit:family:" + familyId;
    }

    public static String familySubLimit(Long familyId, Long subId) {
        return "limit:family_sub:" + familyId + ":" + subId;
    }

    public static String familyUsage(Long familyId) {
        return "usage:family:" + familyId + ":" + currentYYYYMM();
    }

    public static String subLimit(Long subId) {
        return "limit:sub:" + subId;
    }

    public static String subUsage(Long subId) {
        return "usage:sub:" + subId + ":" + currentYYYYMM();
    }
}
