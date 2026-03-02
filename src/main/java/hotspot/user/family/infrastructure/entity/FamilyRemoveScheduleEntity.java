package hotspot.user.family.infrastructure.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import hotspot.user.common.BaseEntity;
import hotspot.user.family.domain.DeleteStatus;
import hotspot.user.family.domain.FamilyRemoveSchedule;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가족 삭제 스케쥴러 entity
 */

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "family_remove_schedule")
public class FamilyRemoveScheduleEntity extends BaseEntity {

    @Id
    @Column(name = "family_remove_schedule_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long targetSubId;
    private Long familyId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeleteStatus status = DeleteStatus.SCHEDULED;

    private LocalDate scheduleDate;

    public static FamilyRemoveScheduleEntity domainToEntity(FamilyRemoveSchedule familyRemoveSchedule) {
        return FamilyRemoveScheduleEntity.builder()
                .id(familyRemoveSchedule.getId())
                .targetSubId(familyRemoveSchedule.getTargetSubId())
                .familyId(familyRemoveSchedule.getFamilyId())
                .status(familyRemoveSchedule.getStatus())
                .scheduleDate(familyRemoveSchedule.getScheduleDate())
                .build();
    }

    public FamilyRemoveSchedule entityToDomain() {
        return FamilyRemoveSchedule.builder()
                .id(this.id)
                .targetSubId(this.targetSubId)
                .familyId(this.familyId)
                .status(this.status)
                .scheduleDate(this.scheduleDate)
                .build();
    }


}
