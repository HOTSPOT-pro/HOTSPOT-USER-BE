package hotspot.user.usage.reportUsage.controller.port;

import hotspot.user.usage.reportUsage.controller.response.ReportUsageAppResponse;

public interface FindReportUsageAppMonthService {

    ReportUsageAppResponse findReportUsageAppMonth(Long memberId, Long targetSubId);
}
