package hotspot.user.plan.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import hotspot.user.common.BaseEntity;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.plan.domain.Plan;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 실제 요금제 엔티티 (DB 저장)
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "plan")
@SQLDelete(sql = "UPDATE plan SET is_deleted = true WHERE plan_id = ?")
@Where(clause = "is_deleted = false")
public class PlanEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @Column(length = 20, nullable = false)
    private String planName;

    private Long planDataAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DataPeriod dataPeriod = DataPeriod.MONTH;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static PlanEntity domainToEntity(Plan plan) {
        return PlanEntity.builder()
                .planId(plan.getId())
                .planName(plan.getName())
                .planDataAmount(plan.getDataAmount())
                .dataPeriod(plan.getDataPeriod())
                .build();
    }

    public Plan entityToDomain() {
        return Plan.builder()
                .id(this.planId)
                .name(this.planName)
                .dataAmount(this.planDataAmount)
                .dataPeriod(this.dataPeriod)
                .build();
    }


}
