package hotspot.user.dispatch.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Builder;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Builder
public class SsePayload {
    private Long notificationId;
    private String notificationType;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Long unreadCount;
}
