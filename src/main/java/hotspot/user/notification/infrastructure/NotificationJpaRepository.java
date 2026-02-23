package hotspot.user.notification.infrastructure;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.user.notification.infrastructure.entity.NotificationEntity;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {

    // (eventId, subId)가 없을 때만 저장하고, 중복이면 저장하지 않는다.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    INSERT INTO notification (
                        sub_id,
                        event_id,
                        notification_type,
                        notification_content,
                        is_read,
                        created_time
                    ) VALUES (
                        :subId,
                        :eventId,
                        :notificationType,
                        :content,
                        false,
                        :createdTime
                    )
                    ON CONFLICT (event_id, sub_id)
                    DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertIgnoreConflict(
            @Param("subId") Long subId,
            @Param("eventId") String eventId,
            @Param("notificationType") String notificationType,
            @Param("content") String content,
            @Param("createdTime") LocalDateTime createdTime
    );

    // created_time이 기준 시각 이상인 알림을 최신순으로 조회한다.
    Page<NotificationEntity> findBySubscriptionSubIdAndCreatedTimeGreaterThanEqualOrderByCreatedTimeDesc(
            Long subId,
            LocalDateTime cutoffDateTime,
            Pageable pageable
    );

    // 안 읽은 알림의 개수를 카운트한다.
    long countBySubscriptionSubIdAndIsReadFalse(Long subId);

    // 해당 회선의 읽지 않은 알림을 모두 읽음 처리한다.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE NotificationEntity n
               SET n.isRead = true
             WHERE n.subscription.subId = :subId
               AND n.isRead = false
            """)
    int markAllReadBySubId(@Param("subId") Long subId);

    // 해당 회선 소유의 특정 알림 1건만 읽음 처리한다.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE NotificationEntity n
               SET n.isRead = true
             WHERE n.notificationId = :notificationId
               AND n.subscription.subId = :subId
               AND n.isRead = false
            """)
    int markReadByIdAndSubId(
            @Param("notificationId") Long notificationId,
            @Param("subId") Long subId
    );
}
