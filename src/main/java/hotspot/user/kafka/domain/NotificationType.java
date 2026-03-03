package hotspot.user.kafka.domain;

import java.util.EnumSet;
import java.util.Set;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.NotificationErrorCode;
import hotspot.user.notification.domain.NotificationCategory;

public enum NotificationType {
    SINGLE_USAGE_THRESHOLD_50(NotificationCategory.DATA),
    SINGLE_USAGE_THRESHOLD_30(NotificationCategory.DATA),
    SINGLE_USAGE_THRESHOLD_10(NotificationCategory.DATA),
    SINGLE_USAGE_EXHAUSTED(NotificationCategory.DATA),
    FAMILY_USAGE_THRESHOLD_50(NotificationCategory.DATA),
    FAMILY_USAGE_THRESHOLD_30(NotificationCategory.DATA),
    FAMILY_USAGE_THRESHOLD_10(NotificationCategory.DATA),
    FAMILY_USAGE_EXHAUSTED(NotificationCategory.DATA),
    PRESENT_USAGE_THRESHOLD_50(NotificationCategory.PRESENT),
    PRESENT_USAGE_THRESHOLD_30(NotificationCategory.PRESENT),
    PRESENT_USAGE_THRESHOLD_10(NotificationCategory.PRESENT),
    PRESENT_USAGE_EXHAUSTED(NotificationCategory.PRESENT),
    TIME_WINDOW_POLICY_APPLIED(NotificationCategory.POLICY),
    TIME_WINDOW_POLICY_RELEASED(NotificationCategory.POLICY),
    IMMEDIATE_BLOCK_APPLIED(NotificationCategory.POLICY),
    IMMEDIATE_BLOCK_RELEASED(NotificationCategory.POLICY),
    SERVICE_ACCESS_BLOCKED(NotificationCategory.APP_SERVICE),
    SERVICE_ACCESS_RELEASED(NotificationCategory.APP_SERVICE),
    PRESENT_DATA(NotificationCategory.PRESENT),
    FAMILY_CREATE_APPROVED(NotificationCategory.POLICY),
    FAMILY_CREATE_REJECTED(NotificationCategory.POLICY),
    FAMILY_MEMBER_ADD_APPROVED(NotificationCategory.POLICY),
    FAMILY_MEMBER_ADD_REJECTED(NotificationCategory.POLICY),
    FAMILY_MEMBER_REMOVE_APPROVED(NotificationCategory.POLICY),
    FAMILY_MEMBER_REMOVE_REJECTED(NotificationCategory.POLICY);

    private static final Set<NotificationType> ALWAYS_ALLOWED_TYPES = EnumSet.of(
            FAMILY_MEMBER_ADD_APPROVED,
            FAMILY_MEMBER_ADD_REJECTED,
            FAMILY_MEMBER_REMOVE_APPROVED,
            FAMILY_MEMBER_REMOVE_REJECTED
    );

    private static final Set<NotificationType> SMS_ALLOWED_TYPES = EnumSet.of(
            SINGLE_USAGE_THRESHOLD_50,
            SINGLE_USAGE_THRESHOLD_30,
            SINGLE_USAGE_THRESHOLD_10,
            SINGLE_USAGE_EXHAUSTED,
            FAMILY_USAGE_THRESHOLD_50,
            FAMILY_USAGE_THRESHOLD_30,
            FAMILY_USAGE_THRESHOLD_10,
            FAMILY_USAGE_EXHAUSTED,
            PRESENT_USAGE_THRESHOLD_50,
            PRESENT_USAGE_THRESHOLD_30,
            PRESENT_USAGE_THRESHOLD_10,
            PRESENT_USAGE_EXHAUSTED,
            FAMILY_CREATE_APPROVED,
            FAMILY_CREATE_REJECTED
    );

    private final NotificationCategory category;

    NotificationType(NotificationCategory category) {
        this.category = category;
    }

    public NotificationCategory category() {
        return category;
    }

    public static NotificationType from(String rawType) {
        try {
            return NotificationType.valueOf(rawType);
        } catch (IllegalArgumentException ex) {
            throw new ApplicationException(NotificationErrorCode.NOTIFICATION_CATEGORY_MAPPING_NOT_FOUND);
        }
    }

    public static boolean isAlwaysAllowed(NotificationType type) {
        return ALWAYS_ALLOWED_TYPES.contains(type);
    }

    public static boolean isAlwaysAllowed(String rawType) {
        return isAlwaysAllowed(from(rawType));
    }

    public static boolean isSmsAllowed(NotificationType type) {
        return SMS_ALLOWED_TYPES.contains(type);
    }

    public static boolean isSmsAllowed(String rawType) {
        return isSmsAllowed(from(rawType));
    }
}
