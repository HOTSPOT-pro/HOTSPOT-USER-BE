package hotspot.user.notification.domain.mapper;

import org.springframework.data.domain.Slice;

import hotspot.user.notification.controller.response.NotificationListResponse;
import hotspot.user.notification.controller.response.NotificationResponse;
import hotspot.user.notification.domain.Notification;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    // Notification 도메인 객체 1건을 API 응답 DTO로 변환한다.
    public static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .eventId(notification.getEventId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .createdTime(notification.getCreatedTime())
                .build();
    }

    // Slice 조회 결과를 알림 리스트 + page/size + hasNext를 포함한 목록 응답으로 변환한다.
    public static NotificationListResponse toListResponse(Slice<Notification> notifications) {
        return NotificationListResponse.builder()
                .notifications(notifications.getContent().stream()
                        .map(NotificationMapper::toResponse)
                        .toList())
                .page(notifications.getNumber())
                .size(notifications.getSize())
                .hasNext(notifications.hasNext())
                .build();
    }
}
