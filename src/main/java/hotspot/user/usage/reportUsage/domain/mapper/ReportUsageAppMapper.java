package hotspot.user.usage.reportUsage.domain.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageAppResponse;
import hotspot.user.usage.reportUsage.domain.AppUsage;

public final class ReportUsageAppMapper {

    private ReportUsageAppMapper() {}

    public static Map<Long, String> toAppIdNameMap(
            List<AppBlockedService> services
    ) {
        return services.stream()
                .collect(Collectors.toMap(
                        AppBlockedService::getId,
                        AppBlockedService::getName
                ));
    }

    public static ReportUsageAppResponse toReportUsageAppResponse(
            LocalDateTime currentDateTime,
            List<AppUsage> appUsages,
            Map<Long, String> appIdToName
    ) {

        List<ReportUsageAppResponse.AppUsageResponse> responses =
                appUsages.stream()
                        .map(app -> new ReportUsageAppResponse.AppUsageResponse(
                                app.getAppId(),
                                appIdToName.getOrDefault(app.getAppId(), "Unknown"),
                                app.getUsedGb()
                        ))
                        .toList();

        return new ReportUsageAppResponse(currentDateTime, responses);
    }
}
