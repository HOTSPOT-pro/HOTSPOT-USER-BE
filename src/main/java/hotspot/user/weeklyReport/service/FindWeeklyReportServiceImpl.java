import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.WeeklyReportErrorCode;
import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.domain.FamilySubscription;
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
    private final FindFamilySubscriptionService familySubscriptionService;

    @Override
    public WeeklyReportResponse findWeeklyReport(Long requesterMemberId, Long subId, Long reportId) {
        // 1. 가족 구성원 권한 체크
        FamilySubscription requester = familySubscriptionService.findByMemberId(requesterMemberId);
        FamilySubscription target = familySubscriptionService.findBySubId(subId);
        requester.validateSameFamily(target);

        // 2. 리포트 조회
        WeeklyReport report = weeklyReportRepository.findByReportId(reportId)
                .orElseThrow(() -> new ApplicationException(WeeklyReportErrorCode.REPORT_NOT_FOUND));

        // 3. 리포트 소유권 체크
        if (!report.getSubId().equals(subId)) {
            throw new ApplicationException(WeeklyReportErrorCode.REPORT_ACCESS_DENIED);
        }

        return WeeklyReportMapper.toWeeklyReportResponse(report);
    }

}
