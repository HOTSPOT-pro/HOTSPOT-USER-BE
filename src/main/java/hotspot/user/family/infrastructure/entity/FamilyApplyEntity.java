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
import jakarta.persistence.Table;

import hotspot.user.common.BaseEntity;
import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.FamilyApply;
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
@Table(name = "family_apply")
public class FamilyApplyEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long familyApplyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_sub_id")
    private SubscriptionEntity requesterSubscription;

    @Column(name = "family_id")
    private Long familyId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplyType applyType;

    private String docUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplyStatus status = ApplyStatus.PENDING;

    public static FamilyApplyEntity domainToEntity(FamilyApply familyApply) {

        // ID만 가지고 있는 프록시 객체 생성
        SubscriptionEntity requesterSub = SubscriptionEntity.builder()
                .subId(familyApply.getRequesterSubId())
                .build();

        return FamilyApplyEntity.builder()
                .familyApplyId(familyApply.getId())
                .requesterSubscription(requesterSub)
                .familyId(familyApply.getId())
                .applyType(familyApply.getApplyType())
                .docUrl(familyApply.getDocUrl())
                .status(familyApply.getStatus() != null ? familyApply.getStatus() : ApplyStatus.PENDING)
                .build();
    }


    public FamilyApply entityToDomain() {
        return FamilyApply.builder()
                .id(this.familyApplyId)
                .requesterSubId(this.requesterSubscription != null ?
                        this.requesterSubscription.getSubId() : null) // NPE 방지
                .familyId(familyId)
                .applyType(this.applyType)
                .docUrl(this.docUrl)
                .status(this.status)
                .build();
    }

}
