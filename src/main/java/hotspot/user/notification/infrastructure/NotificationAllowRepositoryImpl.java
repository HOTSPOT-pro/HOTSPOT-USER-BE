package hotspot.user.notification.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.notification.domain.NotificationAllow;
import hotspot.user.notification.domain.NotificationCategory;
import hotspot.user.notification.infrastructure.entity.NotificationAllowEntity;
import hotspot.user.notification.service.port.NotificationAllowRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationAllowRepositoryImpl implements NotificationAllowRepository {

    private final NotificationAllowJpaRepository notificationAllowJpaRepository;

    // 해당 subId의 삭제되지 않은 알림 허용 설정을 전부 조회한 뒤, NotificationAllow로 반환한다.
    @Override
    public List<NotificationAllow> findAllBySubId(Long subId) {
        return notificationAllowJpaRepository.findBySubscriptionSubIdAndIsDeletedFalse(subId).stream()
                .map(NotificationAllowEntity::entityToDomain)
                .toList();
    }

    // 해당 subId에서 특정 카테고리의 삭제되지 않은 알림 허용 설정 1건을 조회하고 있으면 반환한다.
    @Override
    public Optional<NotificationAllow> findBySubIdAndCategory(Long subId, NotificationCategory notificationCategory) {
        return notificationAllowJpaRepository
                .findBySubscriptionSubIdAndNotificationCategoryAndIsDeletedFalse(subId, notificationCategory)
                .map(NotificationAllowEntity::entityToDomain);
    }

    // 도메인 객체를 엔티티로 변환해 DB에 저장한 뒤, 저장된 엔티티를 다시 도메인으로 반환한다.
    @Override
    public NotificationAllow save(NotificationAllow notificationAllow) {
        NotificationAllowEntity entity = NotificationAllowEntity.domainToEntity(notificationAllow);
        NotificationAllowEntity saved = notificationAllowJpaRepository.save(entity);
        return saved.entityToDomain();
    }
}
