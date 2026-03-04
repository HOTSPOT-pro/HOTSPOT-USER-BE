package hotspot.user.policy.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import hotspot.user.common.BaseEntity;
import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 앱차단 서비스 매핑 엔티티 (DB 저장)
 */

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "blocked_service_sub")
public class BlockedServiceSubEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blockedServiceSubId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id")
    private SubscriptionEntity subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_service_id")
    private AppBlockedServiceEntity appBlockedService;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true; // 활성화 여부

    public BlockedServiceSub entityToDomain() {
        return BlockedServiceSub.builder()
                .id(this.blockedServiceSubId)
                .subscription(this.subscription.entityToDomain())
                .appBlockedService(this.appBlockedService.entityToDomain())
                .isActive(this.isActive)
                .build();
    }
}
