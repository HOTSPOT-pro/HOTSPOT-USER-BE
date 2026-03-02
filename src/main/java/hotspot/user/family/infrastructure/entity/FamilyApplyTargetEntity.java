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
import hotspot.user.family.domain.FamilyApplyTarget;
import hotspot.user.member.domain.FamilyRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가족 생성 / 구성원 추가 신청 타겟 엔티티
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "family_apply_target")
public class FamilyApplyTargetEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long familyApplyTargetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_apply_id")
    @Column(nullable = false)
    private FamilyApplyEntity familyApply;

    @Column(name = "target_sub_id", nullable = false)
    private Long targetSubId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FamilyRole targetFamilyRole;

    public static FamilyApplyTargetEntity domainToEntity(FamilyApplyTarget familyApplyTarget) {
        // ID만 가지고 있는 프록시 객체 생성
        FamilyApplyEntity familyApplyProxy = FamilyApplyEntity.builder()
                .familyApplyId(familyApplyTarget.getFamilyApplyId())
                .build();

        return FamilyApplyTargetEntity.builder()
                .familyApply(familyApplyProxy)
                .targetSubId(familyApplyTarget.getTargetSubId())
                .targetFamilyRole(familyApplyTarget.getTargetFamilyRole())
                .build();
    }

    public FamilyApplyTarget entityToDomain() {
        return FamilyApplyTarget.builder()
                .id(this.familyApplyTargetId)
                .familyApplyId(this.familyApply != null ? familyApply.getFamilyApplyId() : null)
                .targetSubId(this.targetSubId)
                .targetFamilyRole(this.targetFamilyRole)
                .build();
    }

}
