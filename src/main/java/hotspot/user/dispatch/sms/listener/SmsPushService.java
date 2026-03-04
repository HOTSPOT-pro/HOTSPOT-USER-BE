package hotspot.user.dispatch.sms.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.dispatch.sms.config.SmsProperties;
import hotspot.user.dispatch.sms.infrastructure.SmsDispatchQueuePublisher;
import hotspot.user.kafka.dto.UserAlertNotificationsPersistedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsPushService {

    private final SmsProperties smsProperties;
    private final SmsDispatchQueuePublisher smsDispatchQueuePublisher;

    @EventListener
    // 저장된 알림 이벤트를 받아 SMS 디스패치 큐에 적재한다.
    public void onNotificationsPersisted(UserAlertNotificationsPersistedEvent event) {
        if (!smsProperties.isEnabled()) {
            return;
        }

        if (!smsProperties.isEnabled2()) {
            return;
        }

        if (!smsProperties.isEnabled3()) {
            return;
        }

        if (!smsProperties.isEnabled4()) {
            return;
        }

        if (!smsProperties.isEnabled5()) {
            return;
        }

        for (var notification : event.persistedNotifications()) {
            try {
                smsDispatchQueuePublisher.enqueue(notification);
            } catch (Exception ex) {
                log.error(
                        "Failed to enqueue SMS dispatch. notificationId={}, subId={}, reason={}",
                        notification.getId(),
                        notification.getSubId(),
                        ex.getMessage(),
                        ex
                );
                throw new ApplicationException(SmsErrorCode.SMS_LISTENER_FAILED, ex);
            }
        }
    }
}
