package hotspot.user.dispatch.sms.dto;

import java.time.LocalDateTime;

import hotspot.user.notification.domain.Notification;

public record SmsDispatchCommand(
        Long notificationId,
        Long subId,
        String eventId,
        String notificationType,
        String title,
        String content,
        LocalDateTime createdTime
) {
    // Notification 도메인 객체를 큐 전송용 커맨드로 변환한다.
    public static SmsDispatchCommand from(Notification notification) {
        return new SmsDispatchCommand(
                notification.getId(),
                notification.getSubId(),
                notification.getEventId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getCreatedTime()
        );
    }

    // 큐에서 받은 커맨드를 SMS 발송용 Notification 객체로 복원한다.
    public Notification toNotification() {
        return Notification.builder()
                .id(notificationId)
                .subId(subId)
                .eventId(eventId)
                .notificationType(notificationType)
                .title(title)
                .content(content)
                .isRead(false)
                .createdTime(createdTime)
                .build();
    }
}
