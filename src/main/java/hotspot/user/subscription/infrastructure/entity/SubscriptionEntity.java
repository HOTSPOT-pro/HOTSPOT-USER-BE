package hotspot.user.subscription.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import hotspot.user.common.BaseEntity;
import hotspot.user.member.infrastructure.entity.MemberEntity;
import hotspot.user.plan.infrastructure.entity.PlanEntity;
import hotspot.user.subscription.domain.Subscription;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 실제 회선 엔티티 (DB 저장)
 */

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "subscription")
@SQLDelete(sql = "UPDATE subscription SET is_deleted = true WHERE sub_id = ?")
@Where(clause = "is_deleted = false")
public class SubscriptionEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private PlanEntity plan;

    // [To-Do] 나중에는 회원 당 여러 회선 고려해 @ManyToOne으로 바꾸기
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @Column(nullable = false)
    private String phoneEnc; // 암호화된 전화번호

    @Column(length = 64, nullable = false)
    private String phoneHash; // 조회 해시

    @Column(nullable = false)
    @Builder.Default
    private Boolean isLocked = false; // 차단 여부

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static SubscriptionEntity domainToEntity(Subscription subscription) {
        return SubscriptionEntity.builder()
                .subId(subscription.getId())
                .member(subscription.getMember() != null ? MemberEntity.domainToEntity(subscription.getMember()) : null)
                .plan(subscription.getPlan() != null ? PlanEntity.domainToEntity(subscription.getPlan()) : null)
                .phoneEnc(subscription.getPhoneEnc())
                .phoneHash(subscription.getPhoneHash())
                .isLocked(subscription.getIsLocked())
                .isDeleted(false)
                .build();
    }

    public Subscription entityToDomain() {
        return Subscription.builder()
                .id(this.subId)
                .member(this.member != null ? this.member.entityToDomain() : null)
                .plan(this.plan != null ? this.plan.entityToDomain() : null)
                .phoneEnc(this.phoneEnc)
                .phoneHash(this.phoneHash)
                .isLocked(this.isLocked)
                .build();
    }
}
