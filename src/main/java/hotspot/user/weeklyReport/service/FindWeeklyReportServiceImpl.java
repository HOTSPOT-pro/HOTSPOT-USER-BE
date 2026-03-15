package hotspot.user.weeklyReport.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import hotspot.user.weeklyReport.service.port.WeeklyReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.weeklyReport.controller.port.FindWeeklyReportService;
import hotspot.user.weeklyReport.controller.response.WeeklyReportResponse;
import hotspot.user.weeklyReport.domain.AIFeedback;
import hotspot.user.weeklyReport.domain.ScoreData;
import hotspot.user.weeklyReport.domain.SummaryData;
import hotspot.user.weeklyReport.domain.UsageListData;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindWeeklyReportServiceImpl implements FindWeeklyReportService {
    private final WeeklyReportRepository weeklyReportRepository;

    @Override
    public WeeklyReportResponse findWeeklyReport(Long memberId, Long subId, Long reportId) {
        return WeeklyReportResponse.builder().build();
    }
}
