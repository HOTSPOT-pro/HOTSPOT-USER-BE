package hotspot.user.usage.reportUsage.controller.port;

import hotspot.user.usage.reportUsage.controller.response.ReportUsageMonthResponse;

public interface FindReportUsageMonthService {

    ReportUsageMonthResponse findReportUsageMonth(
            Long familyId,
            Long targetSubId
    );
}
