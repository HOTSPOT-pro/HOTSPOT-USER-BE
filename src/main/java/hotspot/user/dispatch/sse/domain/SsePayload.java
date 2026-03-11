package hotspot.user.dispatch.sse.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import hotspot.user.notification.domain.NotificationCategory;
import lombok.Builder;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Builder
public class SsePayload {
    private Long notificationId;
    private String notificationType;
    private String title;
    private String content;
    private LocalDateTime createdTime;
    private NotificationCategory notificationCategory;
    private Long unreadCount;
}
