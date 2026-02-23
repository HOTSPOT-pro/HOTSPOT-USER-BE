package hotspot.user.notification.controller.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Builder;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Builder
public class NotificationResponse {
    private Long id;
    private String eventId;
    private String notificationType;
    private String content;
    private boolean isRead;
    private LocalDateTime createdTime;
}
