package hotspot.user.notification.controller.port;

import org.springframework.data.domain.Pageable;

import hotspot.user.notification.controller.response.NotificationListResponse;
import hotspot.user.notification.controller.response.UnreadNotificationCountResponse;

public interface NotificationService {

    // 인증 사용자 기준으로 최근 알림 목록을 반환한다.
    NotificationListResponse findNotifications(Long memberId, Pageable pageable);

    // 인증 사용자 기준으로 안 읽은 알림 수를 반환한다.
    UnreadNotificationCountResponse findUnreadCount(Long memberId);

    // 인증 사용자 기준으로 모든 알림을 읽음 처리한다.
    void markAllRead(Long memberId);

    // 인증 사용자 기준으로 특정 알림 1건을 읽음 처리한다.
    void markRead(Long memberId, Long notificationId);
}
