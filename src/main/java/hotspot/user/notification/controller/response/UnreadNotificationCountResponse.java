package hotspot.user.notification.controller.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Builder;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Builder
public class UnreadNotificationCountResponse {
    private long unreadCount;
}
