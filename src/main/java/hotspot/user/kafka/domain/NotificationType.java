package hotspot.user.kafka.domain;

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
    PRESENT_DATA(NotificationCategory.PRESENT);

    private final NotificationCategory category;

    NotificationType(NotificationCategory category) {
        this.category = category;
    }

    public NotificationCategory category() {
        return category;
    }
}
