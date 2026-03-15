package hotspot.user.familyReport.infrastructure.entity;

import java.time.DayOfWeek;

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
import hotspot.user.family.infrastructure.entity.FamilyEntity;
import hotspot.user.familyReport.domain.FamilyReport;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "family_report")
public class FamilyReportEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long familyReportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private FamilyEntity family;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private DayOfWeek receiveDay;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public static FamilyReportEntity domainToEntity(FamilyReport familyReport) {
        return FamilyReportEntity.builder()
                .familyReportId(familyReport.getId())
                .family(FamilyEntity.builder()
                        .familyId(familyReport.getFamily().getId())
                        .build())
                .receiveDay(familyReport.getReceiveDay())
                .isActive(familyReport.isActive())
                .build();
    }

    public FamilyReport entityToDomain() {
        return FamilyReport.builder()
                .id(this.familyReportId)
                .family(this.family.entityToDomain())
                .receiveDay(this.receiveDay)
                .isActive(Boolean.TRUE.equals(this.isActive))
                .createdTime(getCreatedTime())
                .modifiedTime(getModifiedTime())
                .build();
    }
}
