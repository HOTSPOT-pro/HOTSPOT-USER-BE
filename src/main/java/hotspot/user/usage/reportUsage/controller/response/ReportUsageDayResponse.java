package hotspot.user.usage.reportUsage.controller.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReportUsageDayResponse(
    LocalDateTime currentDateTime,
    List<SubUsageResponse> subUsages
) {
    public record SubUsageResponse(
            Long subId,
            String subName,
            List<DataUsageDayResponse> dataUsageDays
    ) {
        public record DataUsageDayResponse(
                LocalDate usageDate,
                Double usageDayAmount
        ) {
        }
    }
}
