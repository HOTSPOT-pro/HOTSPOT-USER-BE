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

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import hotspot.user.common.BaseEntity;
import hotspot.user.policy.domain.DateSnapshot;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 정책 - 회선 매핑 엔티티 (DB 저장)
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "policy_sub")
@SQLDelete(sql = "UPDATE policy_sub SET is_deleted = true WHERE policy_sub_id = ?")
@Where(clause = "is_deleted = false")
public class PolicySubEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policySubId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id")
    private SubscriptionEntity subscription;

    @Column(name = "policy_id")
    private Long policyId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb") // PostgreSQL
    private DateSnapshot dateSnapshot;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public PolicySub entityToDomain() {
        return PolicySub.builder()
                .id(this.policySubId)
                .policyId(this.policyId)
                .subId(this.subscription.entityToDomain().getId())
                .dateSnapshot(this.dateSnapshot)
                .isDeleted(this.isDeleted)
                .build();
    }

    public static PolicySubEntity domainToEntity(PolicySub policySub) {

        // 연관관계(FK) 매핑을 위한 프록시(가짜) 엔티티 생성
        // DB에서 전체 데이터를 읽어올 필요 없이, 외래키로 쓸 ID값만 세팅
        SubscriptionEntity subscriptionProxy = SubscriptionEntity.builder()
                .subId(policySub.getSubId()) // 도메인이 들고 있는 ID만 주입
                .build();

        return PolicySubEntity.builder()
                .policySubId(policySub.getId())
                .policyId(policySub.getPolicyId())
                .subscription(subscriptionProxy)
                .dateSnapshot(policySub.getDateSnapshot())
                .isDeleted(policySub.getIsDeleted())
                .build();
    }
}
