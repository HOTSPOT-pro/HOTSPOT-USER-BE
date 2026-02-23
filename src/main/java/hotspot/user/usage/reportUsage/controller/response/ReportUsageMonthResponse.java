package hotspot.user.usage.reportUsage.controller.response;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public record ReportUsageMonthResponse(
        LocalDateTime currentDateTime,
        List<SubUsageMonthResponse> subUsages
) {

    public record SubUsageMonthResponse(
            Long subId,
            String subName,
            List<DataUsageMonthResponse> dataUsageMonths
    ) {

        public record DataUsageMonthResponse(
                YearMonth usageMonth,
                Double usageAmount
        ) {}
    }
}
