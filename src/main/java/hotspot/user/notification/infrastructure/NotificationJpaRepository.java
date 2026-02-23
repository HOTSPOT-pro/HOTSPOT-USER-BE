package hotspot.user.notification.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.notification.infrastructure.entity.NotificationEntity;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {

    boolean existsByEventIdAndSubscriptionSubId(String eventId, Long subId);
}
