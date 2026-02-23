package hotspot.user.usage.reportUsage.controller.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReportUsageAppResponse(
        LocalDateTime currentDateTime,
        List<AppUsageResponse> appUsages
) {

    public record AppUsageResponse(
            Long appId,
            String appName,
            Double appDataUsageAmount
    ) {
    }
}
