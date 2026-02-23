package hotspot.user.usage.reportUsage.controller.port;

import hotspot.user.usage.reportUsage.controller.response.ReportUsageAppResponse;

public interface FindReportUsageAppDayService {

    ReportUsageAppResponse findReportUsageAppDay(Long memberId);
}
