package hotspot.user.dispatch.sse.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hotspot.user.dispatch.sse.registry.SseEmitterRegistry;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.dto.UserAlertNotificationsPersistedEvent;
import hotspot.user.notification.domain.Notification;
import hotspot.user.notification.service.port.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class SsePushServiceTest {

    @Mock
    private SseEmitterRegistry sseEmitterRegistry;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private SsePushService notificationSsePushService;

    @Test
    @DisplayName("pushes persisted notifications to all emitters in subId and includes unread count lookup")
    void pushPersistedNotifications() {
        Notification first = notification(101L, 1L, "TYPE_1");
        Notification second = notification(102L, 1L, "TYPE_2");
        UserAlertNotificationsPersistedEvent event = new UserAlertNotificationsPersistedEvent(
                sourceEvent(),
                List.of(first, second)
        );

        List<SseEmitterRegistry.RegisteredEmitter> emitters = List.of(
                new SseEmitterRegistry.RegisteredEmitter("e-1", new SseEmitter()),
                new SseEmitterRegistry.RegisteredEmitter("e-2", new SseEmitter())
        );
        given(sseEmitterRegistry.findBySubId(1L)).willReturn(emitters);
        given(notificationRepository.countUnreadBySubId(1L)).willReturn(7L);

        notificationSsePushService.onNotificationsPersisted(event);

        then(notificationRepository).should().countUnreadBySubId(1L);
        then(sseEmitterRegistry).should(times(2)).findBySubId(1L);
        then(sseEmitterRegistry).should(times(4)).sendAndCleanupOnFailure(any(), any());
        then(sseEmitterRegistry).should(times(2)).sendAndCleanupOnFailure(eq(emitters.get(0)), any());
        then(sseEmitterRegistry).should(times(2)).sendAndCleanupOnFailure(eq(emitters.get(1)), any());
    }

    private UserAlertEvent sourceEvent() {
        return new UserAlertEvent(
                "alert-1",
                "USAGE_THRESHOLD",
                "PLAN_REMAINING",
                1L,
                null,
                "30",
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 2, 23, 10, 15, 30)
        );
    }

    private Notification notification(Long id, Long subId, String type) {
        return Notification.builder()
                .id(id)
                .subId(subId)
                .eventId("evt-" + id)
                .notificationType(type)
                .title("title-" + id)
                .content("test-" + id)
                .isRead(false)
                .createdTime(LocalDateTime.of(2026, 2, 23, 10, 15, 30))
                .build();
    }
}
