package hotspot.user.weeklyReport.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.WeeklyReportErrorCode;
import hotspot.user.weeklyReport.controller.port.FindWeeklyReportService;
import hotspot.user.weeklyReport.controller.response.WeeklyReportResponse;
import hotspot.user.weeklyReport.domain.WeeklyReport;
import hotspot.user.weeklyReport.domain.mapper.WeeklyReportMapper;
import hotspot.user.weeklyReport.service.port.WeeklyReportRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindWeeklyReportServiceImpl implements FindWeeklyReportService {

    private final WeeklyReportRepository weeklyReportRepository;

    @Override
    public WeeklyReportResponse findWeeklyReport(Long memberId, Long subId, Long reportId) {
        WeeklyReport report = weeklyReportRepository.findByReportId(reportId)
                .orElseThrow(() -> new ApplicationException(WeeklyReportErrorCode.REPORT_NOT_FOUND));

        return WeeklyReportMapper.toWeeklyReportResponse(report);
    }

}
