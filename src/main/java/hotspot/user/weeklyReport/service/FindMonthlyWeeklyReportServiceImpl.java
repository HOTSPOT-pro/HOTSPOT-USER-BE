package hotspot.user.weeklyReport.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.weeklyReport.controller.port.FindMonthlyWeeklyReportService;
import hotspot.user.weeklyReport.controller.response.MonthlyWeeklyReportResponse;
import hotspot.user.weeklyReport.domain.WeeklyReport;
import hotspot.user.weeklyReport.domain.WeeklyReportTitleFormatter;
import hotspot.user.weeklyReport.service.port.WeeklyReportRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindMonthlyWeeklyReportServiceImpl implements FindMonthlyWeeklyReportService {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final WeeklyReportRepository weeklyReportRepository;
    private final FindFamilySubscriptionService familySubscriptionService;

    @Override
    public MonthlyWeeklyReportResponse findMonthlyWeeklyReports(
            Long requesterMemberId,
            Long subId,
            YearMonth yearMonth
    ) {
        FamilySubscription requester = familySubscriptionService.findByMemberId(requesterMemberId);
        FamilySubscription target = familySubscriptionService.findBySubId(subId);
        requester.validateSameFamily(target);

        List<WeeklyReport> reports = weeklyReportRepository.findMonthlyReportsBySubId(subId, yearMonth);

        return MonthlyWeeklyReportResponse.builder()
                .subId(subId)
                .name(target.getSubscription().getMember().getName())
                .yearMonth(yearMonth)
                .reports(reports.stream()
                        .map(this::toReportItem)
                        .toList())
                .build();
    }

    private MonthlyWeeklyReportResponse.ReportItem toReportItem(WeeklyReport report) {
        return MonthlyWeeklyReportResponse.ReportItem.builder()
                .reportId(report.getWeeklyReportId())
                .title(WeeklyReportTitleFormatter.format(report))
                .period(createPeriod(report.getWeekStartDate(), report.getWeekEndDate()))
                .weekStartDate(report.getWeekStartDate())
                .weekEndDate(report.getWeekEndDate())
                .reportStatus(report.getReportStatus())
                .build();
    }

    private String createPeriod(LocalDate weekStartDate, LocalDate weekEndDate) {
        return "%s~%s".formatted(
                weekStartDate.format(PERIOD_FORMATTER),
                weekEndDate.format(PERIOD_FORMATTER)
        );
    }
}
