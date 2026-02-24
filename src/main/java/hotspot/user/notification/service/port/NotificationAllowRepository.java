package hotspot.user.notification.service.port;

import java.util.List;
import java.util.Optional;

import hotspot.user.notification.domain.NotificationAllow;
import hotspot.user.notification.domain.NotificationCategory;

public interface NotificationAllowRepository {
    List<NotificationAllow> findAllBySubId(Long subId);

    Optional<NotificationAllow> findBySubIdAndCategory(Long subId, NotificationCategory notificationCategory);

    NotificationAllow save(NotificationAllow notificationAllow);
}
