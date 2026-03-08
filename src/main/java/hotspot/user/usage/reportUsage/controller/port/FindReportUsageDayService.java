package hotspot.user.usage.reportUsage.controller.port;

import java.time.YearMonth;

import hotspot.user.usage.reportUsage.controller.response.ReportUsageDayResponse;

public interface FindReportUsageDayService {

    ReportUsageDayResponse findReportUsageDay(Long familyId, Long targetSubId, YearMonth month);
}
