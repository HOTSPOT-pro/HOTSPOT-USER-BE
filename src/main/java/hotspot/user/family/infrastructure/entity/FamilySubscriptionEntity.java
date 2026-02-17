package hotspot.user.family.infrastructure.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 실제 가족-회선 매핑 엔티티 (DB 저장)
 */

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "family_sub")
public class FamilySubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long familySubId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id")
    private SubscriptionEntity subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private FamilyEntity family;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FamilyRole familyRole = FamilyRole.CHILD;

    @Column(nullable = false)
    @Builder.Default
    private int priority = -1;

    @Column(nullable = false)
    @Builder.Default
    private int dataLimit = -1;

    public static FamilySubscriptionEntity domainToEntity(FamilySubscription familySubscription) {
        return FamilySubscriptionEntity.builder()
                .familySubId(familySubscription.getId())
                .family(FamilyEntity.domainToEntity(familySubscription.getFamily()))
                .subscription(SubscriptionEntity.domainToEntity(familySubscription.getSubscription()))
                .familyRole(familySubscription.getFamilyRole())
                .priority(familySubscription.getPriority())
                .dataLimit(familySubscription.getDataLimit())
                .build();
    }

    public FamilySubscription entityToDomain() {
        return FamilySubscription.builder()
                .id(this.familySubId)
                .family(this.family.entityToDomain())
                .subscription(this.subscription.entityToDomain())
                .familyRole(this.familyRole)
                .priority(this.priority)
                .dataLimit(this.dataLimit)
                .build();
    }
}
