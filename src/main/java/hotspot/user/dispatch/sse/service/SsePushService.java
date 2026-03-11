package hotspot.user.dispatch.sse.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.dispatch.sse.domain.SsePayload;
import hotspot.user.dispatch.sse.registry.SseEmitterRegistry;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.dto.UserAlertNotificationsPersistedEvent;
import hotspot.user.notification.domain.Notification;
import hotspot.user.notification.service.port.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SsePushService {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void onNotificationsPersisted(UserAlertNotificationsPersistedEvent event) {
        Map<Long, Long> unreadCountsBySubId = new HashMap<>();
        for (Notification notification : event.persistedNotifications()) {
            String rawNotificationType = notification.getNotificationType();
            NotificationType notificationType;
            try {
                notificationType = NotificationType.from(rawNotificationType);
            } catch (ApplicationException ex) {
                log.warn(
                        "Skip SSE push due to unknown notification type. notificationId={}, type={}",
                        notification.getId(),
                        rawNotificationType
                );
                continue;
            }

            if (notificationType == NotificationType.FAMILY_CREATE_APPROVED
                    || notificationType == NotificationType.FAMILY_CREATE_REJECTED) {
                continue;
            }

            Long unreadCount = unreadCountsBySubId.computeIfAbsent(
                    notification.getSubId(),
                    notificationRepository::countUnreadBySubId
            );

            SsePayload payload = SsePayload.builder()
                    .notificationId(notification.getId())
                    .notificationType(rawNotificationType)
                    .notificationCategory(notificationType.category())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .createdTime(notification.getCreatedTime())
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
