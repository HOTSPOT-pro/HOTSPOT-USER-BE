package hotspot.user.member.infrastructure.entity;

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

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import hotspot.user.common.BaseEntity;
import hotspot.user.member.domain.Provider;
import hotspot.user.member.domain.SocialAccount;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SocialAccountEntity
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "social_account")
@SQLDelete(sql = "UPDATE social_account SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class SocialAccountEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @Column(length = 50, nullable = false)
    private String email;

    @Column(nullable = false)
    private String socialId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static SocialAccountEntity domainToEntity(SocialAccount socialAccount, MemberEntity memberEntity) {
        return SocialAccountEntity.builder()
                .id(socialAccount.getId())
                .member(memberEntity)
                .email(socialAccount.getEmail())
                .socialId(socialAccount.getSocialId())
                .provider(socialAccount.getProvider())
                .isDeleted(false)
                .build();
    }

    public SocialAccount entityToDomain() {
        return SocialAccount.builder()
                .id(this.id)
                .memberId(this.member.getId())
                .email(this.email)
                .socialId(this.socialId)
                .provider(this.provider)
                .build();
    }
}
