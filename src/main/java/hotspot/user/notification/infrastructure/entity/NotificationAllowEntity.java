package hotspot.user.notification.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import hotspot.user.common.BaseEntity;
import hotspot.user.notification.domain.NotificationAllow;
import hotspot.user.notification.domain.NotificationCategory;
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
@Table(name = "notification_allow")
public class NotificationAllowEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_allow_id")
    private Long notificationAllowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id", nullable = false)
    private SubscriptionEntity subscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_category", nullable = false)
    private NotificationCategory notificationCategory;

    @Column(name = "notification_allow", nullable = false)
    private Boolean notificationAllow;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static NotificationAllowEntity domainToEntity(NotificationAllow notificationAllow) {
        SubscriptionEntity subscriptionProxy = SubscriptionEntity.builder()
                .subId(notificationAllow.getSubId())
                .build();

        return NotificationAllowEntity.builder()
                .notificationAllowId(notificationAllow.getId())
                .subscription(subscriptionProxy)
                .notificationCategory(notificationAllow.getNotificationCategory())
                .notificationAllow(notificationAllow.getNotificationAllow())
                .isDeleted(notificationAllow.getIsDeleted())
                .build();
    }

    public NotificationAllow entityToDomain() {
        return NotificationAllow.builder()
                .id(this.notificationAllowId)
                .subId(this.subscription != null ? this.subscription.getSubId() : null)
                .notificationCategory(this.notificationCategory)
                .notificationAllow(this.notificationAllow)
                .isDeleted(this.isDeleted)
                .build();
    }
}
