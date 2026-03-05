package hotspot.user.dispatch.sms.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.notification.domain.Notification;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SmsDispatchCommand(
        Long notificationId,
        Long subId,
        String eventId,
        String notificationType,
        String title,
        String content,
        LocalDateTime createdTime,
        String planName,
        String providedAmount,
        String usedPercent,
        String usedAmount,
        String presentSenderName
) {
    // Notification 도메인 객체를 큐 전송용 커맨드로 변환한다.
    public static SmsDispatchCommand from(
            Notification notification,
            UserAlertEvent sourceEvent,
            String planName,
            String presentSenderName
    ) {
        return new SmsDispatchCommand(
                notification.getId(),
                notification.getSubId(),
                notification.getEventId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getCreatedTime(),
                planName,
                sourceEvent.providedAmount(),
                sourceEvent.usedPercent(),
                sourceEvent.usedAmount(),
                presentSenderName
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
