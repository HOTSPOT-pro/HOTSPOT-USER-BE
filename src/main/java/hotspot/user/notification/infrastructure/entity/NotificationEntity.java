package hotspot.user.notification.infrastructure.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "notification",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_event_id_sub_id",
                        columnNames = {"event_id", "sub_id"}
                )
        }
)
public class NotificationEntity {

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

    @Column(name = "notification_title", length = 120)
    private String title;

    @Column(name = "notification_content", length = 255, nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

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
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .createdTime(notification.getCreatedTime())
                .build();
    }

    // JPA 엔티티를 도메인 객체로 변환한다.
    public Notification entityToDomain() {
        return Notification.builder()
                .id(this.notificationId)
                .subId(this.subscription != null ? this.subscription.getSubId() : null)
                .eventId(this.eventId)
                .notificationType(this.notificationType)
                .title(this.title)
                .content(this.content)
                .isRead(this.isRead)
                .createdTime(this.createdTime)
                .build();
    }
}
