package hotspot.user.notification.infrastructure;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import hotspot.user.notification.domain.Notification;
import hotspot.user.notification.infrastructure.entity.NotificationEntity;
import hotspot.user.notification.service.port.NotificationRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private static final long RECENT_DAYS = 30L;

    private final NotificationJpaRepository notificationJpaRepository;

    // 충돌 시 무시하는 native insert 결과를 boolean으로 변환한다.
    @Override
    public Notification insertIfAbsent(Notification notification) {
        int affectedRows = notificationJpaRepository.insertIgnoreConflict(
                notification.getSubId(),
                notification.getEventId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getCreatedTime()
        );
        if (affectedRows == 0) {
            return null;
        }

        return notificationJpaRepository
                .findByEventIdAndSubscriptionSubId(notification.getEventId(), notification.getSubId())
                .map(NotificationEntity::entityToDomain)
                .orElse(null);
    }

    // JPA 페이지 결과를 도메인 페이지로 변환한다.
    @Override
    public Page<Notification> findRecentBySubId(Long subId, Pageable pageable) {
        LocalDateTime cutoffDateTime = LocalDateTime.now().minusDays(RECENT_DAYS);
        return notificationJpaRepository
                .findBySubscriptionSubIdAndCreatedTimeGreaterThanEqualOrderByCreatedTimeDesc(
                        subId,
                        cutoffDateTime,
                        pageable
                )
                .map(NotificationEntity::entityToDomain);
    }

    // 회선 기준 안 읽은 알림 수를 반환한다.
    @Override
    public long countUnreadBySubId(Long subId) {
        return notificationJpaRepository.countBySubscriptionSubIdAndIsReadFalse(subId);
    }

    // 회선 기준 전체 읽음 처리 row 수를 반환한다.
    @Override
    public int markAllReadBySubId(Long subId) {
        return notificationJpaRepository.markAllReadBySubId(subId);
    }

    // 회선 소유 알림 1건 읽음 처리 row 수를 반환한다.
    @Override
    public int markReadById(Long notificationId, Long subId) {
        return notificationJpaRepository.markReadByIdAndSubId(notificationId, subId);
    }
}
