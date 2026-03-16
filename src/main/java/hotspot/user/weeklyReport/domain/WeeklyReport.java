package hotspot.user.weeklyReport.domain;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WeeklyReport {
    private Long weeklyReportId;
    private Long familyId;
    private Long subId;
    private String name;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private ReportStatus reportStatus;
    private Long totalUsage;
    private ScoreData scoreData;
    private List<ReportTag> tags;
    private SummaryData summaryData;
    private UsageListData usageListData;
    private AIFeedback aiFeedback;
}
