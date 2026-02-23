package hotspot.user.notification.controller.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Builder;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Builder
public class NotificationListResponse {
    private List<NotificationResponse> notifications;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
}
