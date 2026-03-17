package hotspot.user.weeklyReport.infrastructure.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import hotspot.user.common.BaseEntity;
import hotspot.user.weeklyReport.domain.AIFeedback;
import hotspot.user.weeklyReport.domain.ReportStatus;
import hotspot.user.weeklyReport.domain.ReportTag;
import hotspot.user.weeklyReport.domain.ScoreData;
import hotspot.user.weeklyReport.domain.SummaryData;
import hotspot.user.weeklyReport.domain.UsageListData;
import hotspot.user.weeklyReport.domain.WeeklyReport;
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
@Table(name = "weekly_report")
public class WeeklyReportEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weeklyReportId;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "sub_id", nullable = false)
    private Long subId;

    @Column(nullable = false, length = 20)
    private String name;

    private LocalDate weekStartDate;
    private LocalDate weekEndDate;

    @Enumerated(EnumType.STRING)
    private ReportStatus reportStatus;

    private Long totalUsage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private ScoreData scoreData;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)[]")
    private List<ReportTag> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private SummaryData summaryData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private UsageListData usageListData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private AIFeedback aiFeedback;

    private Boolean isLlmUsed;
    private String aiModel;
    private String promptVersion;

    public static WeeklyReportEntity domainToEntity(WeeklyReport weeklyReport) {
        return WeeklyReportEntity.builder()
                .weeklyReportId(weeklyReport.getWeeklyReportId())
                .familyId(weeklyReport.getFamilyId())
                .subId(weeklyReport.getSubId())
                .name(weeklyReport.getName())
                .weekStartDate(weeklyReport.getWeekStartDate())
                .weekEndDate(weeklyReport.getWeekEndDate())
                .reportStatus(weeklyReport.getReportStatus())
                .totalUsage(weeklyReport.getTotalUsage())
                .scoreData(weeklyReport.getScoreData())
                .tags(weeklyReport.getTags())
                .summaryData(weeklyReport.getSummaryData())
                .usageListData(weeklyReport.getUsageListData())
                .aiFeedback(weeklyReport.getAiFeedback())
                .build();
    }

    public WeeklyReport entityToDomain() {
        return WeeklyReport.builder()
                .weeklyReportId(this.weeklyReportId)
                .familyId(this.familyId)
                .subId(this.subId)
                .name(this.name)
                .weekStartDate(this.weekStartDate)
                .weekEndDate(this.weekEndDate)
                .reportStatus(this.reportStatus)
                .totalUsage(this.totalUsage)
                .scoreData(this.scoreData)
                .tags(this.tags)
                .summaryData(this.summaryData)
                .usageListData(this.usageListData)
                .aiFeedback(this.aiFeedback)
                .build();
    }
}
