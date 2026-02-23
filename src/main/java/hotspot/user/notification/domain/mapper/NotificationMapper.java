package hotspot.user.notification.domain.mapper;

import org.springframework.data.domain.Page;

import hotspot.user.notification.controller.response.NotificationListResponse;
import hotspot.user.notification.controller.response.NotificationResponse;
import hotspot.user.notification.domain.Notification;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    // Converts notification domain to response DTO.
    public static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .eventId(notification.getEventId())
                .notificationType(notification.getNotificationType())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .createdTime(notification.getCreatedTime())
                .build();
    }

    // Converts paged notifications to list response DTO.
    public static NotificationListResponse toListResponse(Page<Notification> notifications) {
        return NotificationListResponse.builder()
                .notifications(notifications.getContent().stream()
                        .map(NotificationMapper::toResponse)
                        .toList())
                .page(notifications.getNumber())
                .size(notifications.getSize())
                .totalPages(notifications.getTotalPages())
                .totalElements(notifications.getTotalElements())
                .hasNext(notifications.hasNext())
                .build();
    }
}
