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
import hotspot.user.member.domain.FamilyRole;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_sub_id")
    private SubscriptionEntity targetSubscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private FamilyEntity family;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplyType applyType;

    @Enumerated(EnumType.STRING)
    private FamilyRole targetFamilyRole;


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

        SubscriptionEntity targetSub = SubscriptionEntity.builder()
                .subId(familyApply.getTargetSubId())
                .build();

        FamilyEntity family = FamilyEntity.builder()
                .familyId(familyApply.getFamilyId())
                .build();

        return FamilyApplyEntity.builder()
                .familyApplyId(familyApply.getId())
                .requesterSubscription(requesterSub)
                .targetSubscription(targetSub)
                .family(family)
                .applyType(familyApply.getApplyType())
                .targetFamilyRole(familyApply.getTargetFamilyRole())
                .docUrl(familyApply.getDocUrl())
                .status(familyApply.getStatus() != null ? familyApply.getStatus() : ApplyStatus.PENDING)
                .build();
    }


    public FamilyApply entityToDomain() {
        return FamilyApply.builder()
                .id(this.familyApplyId)
                .requesterSubId(this.requesterSubscription != null ?
                        this.requesterSubscription.getSubId() : null) // NPE 방지
                .targetSubId(this.targetSubscription != null ?
                        this.targetSubscription.getSubId() : null) // NPE 방지
                .familyId(this.family != null ? this.family.getFamilyId() : null) // NPE 방지
                .applyType(this.applyType)
                .targetFamilyRole(this.targetFamilyRole)
                .docUrl(this.docUrl)
                .status(this.status)
                .build();
    }

}
