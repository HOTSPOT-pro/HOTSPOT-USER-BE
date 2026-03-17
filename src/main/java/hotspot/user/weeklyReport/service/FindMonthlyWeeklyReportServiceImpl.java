package hotspot.user.weeklyReport.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.weeklyReport.controller.port.FindMonthlyWeeklyReportService;
import hotspot.user.weeklyReport.controller.response.MonthlyWeeklyReportResponse;
import hotspot.user.weeklyReport.domain.WeeklyReport;
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
        Map<Long, Integer> weekOrderByReportId = getWeekOrderByReportId(reports);

        return MonthlyWeeklyReportResponse.builder()
                .subId(subId)
                .name(target.getSubscription().getMember().getName())
                .yearMonth(yearMonth)
                .reports(reports.stream()
                        .map(report -> toReportItem(report, yearMonth, weekOrderByReportId))
                        .toList())
                .build();
    }

    private MonthlyWeeklyReportResponse.ReportItem toReportItem(
            WeeklyReport report,
            YearMonth yearMonth,
            Map<Long, Integer> weekOrderByReportId
    ) {
        return MonthlyWeeklyReportResponse.ReportItem.builder()
                .reportId(report.getWeeklyReportId())
                .title(createTitle(yearMonth, weekOrderByReportId.get(report.getWeeklyReportId())))
                .period(createPeriod(report.getWeekStartDate(), report.getWeekEndDate()))
                .weekStartDate(report.getWeekStartDate())
                .weekEndDate(report.getWeekEndDate())
                .reportStatus(report.getReportStatus())
                .build();
    }

    private Map<Long, Integer> getWeekOrderByReportId(List<WeeklyReport> reports) {
        List<WeeklyReport> sortedReports = reports.stream()
                .sorted(Comparator
                        .comparing((WeeklyReport report) -> report.getWeekEndDate().plusDays(1))
                        .thenComparing(WeeklyReport::getWeeklyReportId))
                .toList();

        Map<Long, Integer> weekOrderByReportId = new HashMap<>();

        for (int index = 0; index < sortedReports.size(); index++) {
            weekOrderByReportId.put(sortedReports.get(index).getWeeklyReportId(), index + 1);
        }

        return weekOrderByReportId;
    }

    private String createTitle(YearMonth yearMonth, Integer weekOrder) {
        return "%d년 %d월 %d주차 분석 리포트".formatted(
                yearMonth.getYear(),
                yearMonth.getMonthValue(),
                weekOrder
        );
    }

    private String createPeriod(LocalDate weekStartDate, LocalDate weekEndDate) {
        return "%s~%s".formatted(
                weekStartDate.format(PERIOD_FORMATTER),
                weekEndDate.format(PERIOD_FORMATTER)
        );
    }
}
