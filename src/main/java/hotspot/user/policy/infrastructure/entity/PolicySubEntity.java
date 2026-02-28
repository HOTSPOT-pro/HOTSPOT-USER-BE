package hotspot.user.policy.infrastructure.entity;

import jakarta.persistence.*;

import hotspot.user.common.BaseEntity;
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
// (subId, policyId)에 Unique Key 걸어서 같은 정책이 회선에 2번 적재되지 않도록 방지
@Table(
    name = "policy_sub",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_policy_sub_composite",
            columnNames = {"sub_id", "block_policy_id"}
        )
    }
)
public class PolicySubEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policySubId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id", nullable = false)
    private SubscriptionEntity subscription;

    @Column(name = "sub_id", nullable = false, insertable = false, updatable = false)
    private Long subId; // 조회용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_policy_id", nullable = false)
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
