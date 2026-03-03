package hotspot.user.outbox.notificationOutbox.service.port;

import hotspot.user.outbox.notificationOutbox.domain.event.PolicyAlertOutboxEvent;
import hotspot.user.outbox.notificationOutbox.domain.event.PresentDataGiftedOutboxEvent;
import hotspot.user.outbox.notificationOutbox.domain.event.ServiceAccessAlertOutboxEvent;

public interface UserAlertNotificationOutboxPort {

    void appendPolicyAlert(PolicyAlertOutboxEvent event);

    void appendServiceAccessAlert(ServiceAccessAlertOutboxEvent event);

    void appendPresentDataGiftedAlert(PresentDataGiftedOutboxEvent event);
}
