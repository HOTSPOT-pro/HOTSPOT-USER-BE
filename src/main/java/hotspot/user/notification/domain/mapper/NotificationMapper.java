package hotspot.user.notification.domain.mapper;

import org.springframework.data.domain.Page;

import hotspot.user.notification.controller.response.NotificationListResponse;
import hotspot.user.notification.controller.response.NotificationResponse;
import hotspot.user.notification.domain.Notification;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    // 알림 도메인을 단건 응답 DTO로 변환한다.
    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getEventId(),
                notification.getNotificationType(),
                notification.getContent(),
                notification.getIsRead(),
                notification.getCreatedTime()
        );
    }

    // 알림 페이지를 목록 응답 DTO로 변환한다.
    public static NotificationListResponse toListResponse(Page<Notification> notifications) {
        return new NotificationListResponse(
                notifications.getContent().stream()
                        .map(NotificationMapper::toResponse)
                        .toList(),
                notifications.getNumber(),
                notifications.getSize(),
                notifications.getTotalPages(),
                notifications.getTotalElements(),
                notifications.hasNext()
        );
    }
}
