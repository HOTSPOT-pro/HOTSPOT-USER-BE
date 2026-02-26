package hotspot.user.presentData.infrastructure.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import hotspot.user.presentData.domain.PresentData;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "present_data")
public class PresentDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "present_data_id")
    private Long presentDataId;

    // 선물 받은 회선
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_sub_id", nullable = false)
    private SubscriptionEntity targetSubscription;

    // 선물 준 회선
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provide_sub_id", nullable = false)
    private SubscriptionEntity provideSubscription;

    @Column(name = "data_amount", nullable = false)
    private Long dataAmount;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    public static PresentDataEntity domainToEntity(PresentData presentData) {
        return PresentDataEntity.builder()
                .presentDataId(presentData.getPresentDataId())
                .targetSubscription(SubscriptionEntity.domainToEntity(presentData.getTargetSubscription()))
                .provideSubscription(SubscriptionEntity.domainToEntity(presentData.getProvideSubscription()))
                .dataAmount(presentData.getDataAmount())
                .createdTime(presentData.getCreatedTime())
                .build();
    }

    public PresentData entityToDomain() {
        return PresentData.builder()
                .presentDataId(presentDataId)
                .targetSubscription(targetSubscription.entityToDomain())
                .provideSubscription(provideSubscription.entityToDomain())
                .dataAmount(dataAmount)
                .createdTime(createdTime)
                .build();
    }
}
