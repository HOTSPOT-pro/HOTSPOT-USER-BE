package hotspot.user.presentData.infrastructure.keybuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FamilySubUsageRedisKeyBuilder {

    private static final DateTimeFormatter yyyyMM =
            DateTimeFormatter.ofPattern("yyyyMM");

    private static final DateTimeFormatter yyyyMMdd =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String subUsageMonth(Long subId, LocalDate date) {
        return "usage:sub:" + subId + ":" + date.format(yyyyMM);
    }

    public static String subUsageDay(Long subId, LocalDate date) {
        return "usage:sub:" + subId + ":" + date.format(yyyyMMdd);
    }

    public static String subLimit(Long subId) {
        return "limit:sub:" + subId;
    }
}