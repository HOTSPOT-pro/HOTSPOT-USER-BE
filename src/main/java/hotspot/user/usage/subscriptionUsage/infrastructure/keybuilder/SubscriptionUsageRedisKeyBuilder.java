package hotspot.user.usage.subscriptionUsage.infrastructure.keybuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class SubscriptionUsageRedisKeyBuilder {

    private static final DateTimeFormatter yyyyMM =
            DateTimeFormatter.ofPattern("yyyyMM");

    private SubscriptionUsageRedisKeyBuilder() {}

    private static String formatYYYYMM(LocalDate date) {
        return date.format(yyyyMM);
    }

    // 개인 요금제 한도
    public static String planLimit(Long subId) {
        return "limit:sub:" + subId;
    }

    // 개인 월 사용량
    public static String planUsage(Long subId, LocalDate date) {
        return "usage:sub:" + subId + ":" + formatYYYYMM(date);
    }

    // 선물 인덱스
    public static String giftIndex(Long subId, LocalDate date) {
        return "idx:gift:" + subId + ":" + formatYYYYMM(date);
    }

    // 선물 한도
    public static String giftLimit(Long subId, Long giftId, LocalDate date) {
        return "limit:gift:" + subId + ":" + giftId + ":" + formatYYYYMM(date);
    }

    // 선물 사용량
    public static String giftUsage(Long subId, Long giftId, LocalDate date) {
        return "usage:gift:" + subId + ":" + giftId + ":" + formatYYYYMM(date);
    }
}
