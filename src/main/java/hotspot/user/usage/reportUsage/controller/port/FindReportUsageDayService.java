package hotspot.user.usage.reportUsage.controller.port;

import hotspot.user.usage.reportUsage.controller.response.ReportUsageDayResponse;

public interface FindReportUsageDayService {

    ReportUsageDayResponse findReportUsageDay(Long familyId, Long targetSubId);
}
