package hotspot.user.usage.reportUsage.domain.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hotspot.user.usage.familyUsage.service.schema.FamilySubList;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageDayResponse;

public final class ReportUsageDayMapper {

    private ReportUsageDayMapper() {}

    public static ReportUsageDayResponse toReportUsageDayResponse(
            LocalDateTime now,
            List<LocalDate> dates,
            List<FamilySubList> familySubList,
            Map<Long, Map<LocalDate, Double>> subDailyMap,
            Long targetSubId
    ) {

        Map<Long, String> subIdToName =
                familySubList.stream()
                        .collect(Collectors.toMap(
                                FamilySubList::subId,
                                FamilySubList::subName
                        ));

        Map<LocalDate, Double> familyTotal = new LinkedHashMap<>();

        for (LocalDate d : dates) {

            double sum = 0D;

            for (Map<LocalDate, Double> perSub : subDailyMap.values()) {
                sum += perSub.getOrDefault(d, 0D);
            }

            familyTotal.put(d, sum);
        }

        List<ReportUsageDayResponse.SubUsageResponse> responses =
                new ArrayList<>();

        // 가족 전체
        responses.add(
                toSubUsageResponse(-1L, "가족 전체", dates, familyTotal)
        );

        // targetSubId가 있을 때만 추가
        if (targetSubId != null) {

            responses.add(
                    toSubUsageResponse(
                            targetSubId,
                            subIdToName.get(targetSubId),
                            dates,
                            subDailyMap.getOrDefault(targetSubId, Map.of())
                    )
            );
        }

        return new ReportUsageDayResponse(now, responses);
    }

    private static ReportUsageDayResponse.SubUsageResponse toSubUsageResponse(
            Long subId,
            String subName,
            List<LocalDate> dates,
            Map<LocalDate, Double> dailyMap
    ) {

        List<ReportUsageDayResponse.SubUsageResponse.DataUsageDayResponse> days =
                dates.stream()
                        .map(d -> new ReportUsageDayResponse
                                .SubUsageResponse
                                .DataUsageDayResponse(
                                d,
                                dailyMap.getOrDefault(d, 0D)
                        ))
                        .toList();

        return new ReportUsageDayResponse.SubUsageResponse(
                subId,
                subName,
                days
        );
    }
}
