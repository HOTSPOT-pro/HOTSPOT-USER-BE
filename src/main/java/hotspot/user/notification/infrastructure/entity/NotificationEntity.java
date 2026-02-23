package hotspot.user.notification.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import hotspot.user.common.BaseEntity;
import hotspot.user.notification.domain.Notification;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "notification",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_event_id_sub_id",
                        columnNames = {"event_id", "sub_id"}
                )
        }
)
public class NotificationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id", nullable = false)
    private SubscriptionEntity subscription;

    @Column(name = "event_id", length = 100, nullable = false)
    private String eventId;

    @Column(name = "notification_type", length = 50, nullable = false)
    private String notificationType;

    @Column(name = "notification_content", length = 255, nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    // 도메인 객체를 JPA 엔티티로 변환한다.
    public static NotificationEntity domainToEntity(Notification notification) {
        SubscriptionEntity subscriptionProxy = SubscriptionEntity.builder()
                .subId(notification.getSubId())
                .build();

        return NotificationEntity.builder()
                .notificationId(notification.getId())
                .subscription(subscriptionProxy)
                .eventId(notification.getEventId())
                .notificationType(notification.getNotificationType())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .build();
    }

    // JPA 엔티티를 도메인 객체로 변환한다.
    public Notification entityToDomain() {
        return Notification.builder()
                .id(this.notificationId)
                .subId(this.subscription != null ? this.subscription.getSubId() : null)
                .eventId(this.eventId)
                .notificationType(this.notificationType)
                .content(this.content)
                .isRead(this.isRead)
                .createdTime(this.getCreatedTime())
                .build();
    }
}
