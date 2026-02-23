package hotspot.user.notification.controller.response;

import java.util.List;

public record NotificationListResponse(
        List<NotificationResponse> notifications,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean hasNext
) {
}
