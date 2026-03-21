package hotspot.user.notification.controller.port;

public interface ReadNotificationService {

    // 인증 사용자 기준으로 모든 알림을 읽음 처리한다.
    void markAllRead(Long memberId);

    // 인증 사용자 기준으로 특정 알림 1건을 읽음 처리한다.
    void markRead(Long memberId, Long notificationId);
}
