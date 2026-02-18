package hotspot.user.family.infrastructure.entity;

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
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.PriorityType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 실제 가족 엔티티 (DB 저장)
 */

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "family")
@SQLDelete(sql = "UPDATE family SET is_deleted = true WHERE family_id = ?")
@Where(clause = "is_deleted = false")
public class FamilyEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long familyId;

    @Column(nullable = false)
    @Builder.Default
    private int familyNum = 1;

    @Column(nullable = false)
    @Builder.Default
    private int familyDataAmount = 0;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PriorityType priorityType = PriorityType.FIFO;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public static FamilyEntity domainToEntity(Family family) {
        return FamilyEntity.builder()
                .familyId(family.getId())
                .familyNum(family.getFamilyNum())
                .familyDataAmount(family.getFamilyDataAmount())
                .priorityType(family.getPriorityType())
                .build();
    }

    public Family entityToDomain() {
        return Family.builder()
                .id(this.familyId)
                .familyNum(this.familyNum)
                .familyDataAmount(this.familyDataAmount)
                .priorityType(this.priorityType)
                .build();
    }
}
