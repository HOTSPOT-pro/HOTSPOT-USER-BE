package hotspot.user.dispatch.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hotspot.user.dispatch.domain.SsePayload;
import hotspot.user.dispatch.registry.SseEmitterRegistry;
import hotspot.user.kafka.dto.UserAlertNotificationsPersistedEvent;
import hotspot.user.notification.domain.Notification;
import hotspot.user.notification.service.port.NotificationRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SsePushService {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final NotificationRepository notificationRepository;

    @EventListener
    // 알림이 DB에 저장 완료되면 해당 회선의 모든 SSE 구독자에게 실시간 알림 이벤트를 전송한다.
    public void onNotificationsPersisted(UserAlertNotificationsPersistedEvent event) {
        Map<Long, Long> unreadCountsBySubId = new HashMap<>();
        for (Notification notification : event.persistedNotifications()) {
            Long unreadCount = unreadCountsBySubId.computeIfAbsent(
                    notification.getSubId(),
                    notificationRepository::countUnreadBySubId
            );

            SsePayload payload = SsePayload.builder()
                    .notificationId(notification.getId())
                    .notificationType(notification.getNotificationType())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .createdAt(notification.getCreatedTime())
                    .unreadCount(unreadCount)
                    .build();

            sseEmitterRegistry.findBySubId(notification.getSubId())
                    .forEach(registeredEmitter -> sseEmitterRegistry.sendAndCleanupOnFailure(
                            registeredEmitter,
                            SseEmitter.event()
                                    .name("notification")
                                    .id(String.valueOf(notification.getId()))
                                    .data(payload)
                    ));
        }
    }
}
