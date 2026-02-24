package hotspot.user.notification.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Notification {
    private Long id;
    private Long subId;
    private String eventId;
    private String notificationType;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdTime;
}
