package hotspot.user.usage.reportUsage.controller.port;

import java.time.LocalDate;

import hotspot.user.usage.reportUsage.controller.response.ReportUsageAppResponse;

public interface FindReportUsageAppDayService {

    ReportUsageAppResponse findReportUsageAppDay(Long memberId, Long targetSubId, LocalDate date);
}
