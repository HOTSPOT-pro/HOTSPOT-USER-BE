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

    @Column(name = "sub_id", nullable = false, insertable = false, updatable = false)
    private Long subId; // 조회용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_policy_id")
    private BlockPolicyEntity blockPolicy;

    @Column(name = "block_policy_id", nullable = false, insertable = false, updatable = false)
    private Long blockPolicyId; // 조회용

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public PolicySub entityToDomain() {
        return PolicySub.builder()
                .id(this.policySubId)
                .subId(this.subId)
                .blockPolicyId(this.blockPolicyId)
                .isActive(this.isActive)
                .build();
    }

    public static PolicySubEntity domainToEntity(PolicySub policySub) {

        // 연관관계(FK) 매핑을 위한 프록시 엔티티 생성
        SubscriptionEntity subscriptionProxy = policySub.getSubId() != null ?
                SubscriptionEntity.builder().subId(policySub.getSubId()).build() : null;

        BlockPolicyEntity blockPolicyProxy = policySub.getBlockPolicyId() != null ?
                BlockPolicyEntity.builder().blockPolicyId(policySub.getBlockPolicyId()).build() : null;

        return PolicySubEntity.builder()
                .policySubId(policySub.getId())
                .subscription(subscriptionProxy)
                .blockPolicy(blockPolicyProxy)
                .isActive(policySub.isActive())
                .build();
    }
}
