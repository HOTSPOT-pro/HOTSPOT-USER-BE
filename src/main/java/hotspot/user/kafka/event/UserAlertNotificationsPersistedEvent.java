package hotspot.user.kafka.event;

import java.util.List;

import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.notification.domain.Notification;

public record UserAlertNotificationsPersistedEvent(
        UserAlertEvent sourceEvent,
        List<Notification> persistedNotifications
) {
    // 외부 변경을 막기 위해 저장 성공 목록을 불변 리스트로 고정한다.
    public UserAlertNotificationsPersistedEvent {
        persistedNotifications = List.copyOf(persistedNotifications);
    }
}
