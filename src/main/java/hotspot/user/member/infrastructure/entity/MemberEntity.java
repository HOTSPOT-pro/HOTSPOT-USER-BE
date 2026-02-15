package hotspot.user.member.infrastructure.entity;


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
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.domain.Status;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MemberEntity
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "member")
@SQLDelete(sql = "UPDATE member SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class MemberEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String name;

    @Column(length = 6)
    private String birth;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;


    public static MemberEntity domainToEntity(Member member) {
        return MemberEntity.builder()
                .id(member.getId())
                .name(member.getName())
                .birth(member.getBirth())
                .status(member.getStatus())
                .isDeleted(false)
                .build();
    }

    public Member entityToDomain() {
        return Member.builder()
                .id(this.id)
                .name(this.name)
                .birth(this.birth)
                .status(this.status)
                .build();
    }

    /**
     * SocialAccount를 포함하여 Member 도메인 객체로 변환합니다.
     * RepositoryImpl에서 각 테이블의 데이터를 조회한 후 조립할 때 사용합니다.
     */
    public Member entityToDomain(SocialAccount socialAccount) {
        return Member.builder()
                .id(this.id)
                .name(this.name)
                .birth(this.birth)
                .status(this.status)
                .socialAccount(socialAccount)
                .build();
    }
}
