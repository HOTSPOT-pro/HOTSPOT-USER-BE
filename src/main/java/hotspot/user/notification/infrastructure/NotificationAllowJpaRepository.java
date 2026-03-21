package hotspot.user.notification.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hotspot.user.notification.domain.NotificationCategory;
import hotspot.user.notification.infrastructure.entity.NotificationAllowEntity;

public interface NotificationAllowJpaRepository extends JpaRepository<NotificationAllowEntity, Long> {

    List<NotificationAllowEntity> findBySubscriptionSubIdAndIsDeletedFalse(Long subId);

    Optional<NotificationAllowEntity> findBySubscriptionSubIdAndNotificationCategoryAndIsDeletedFalse(
            Long subId,
            NotificationCategory notificationCategory
    );
}
