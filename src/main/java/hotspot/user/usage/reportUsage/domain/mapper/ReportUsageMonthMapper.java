package hotspot.user.usage.reportUsage.domain.mapper;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hotspot.user.usage.familyUsage.service.schema.FamilySubList;
import hotspot.user.usage.reportUsage.controller.response.ReportUsageMonthResponse;

public final class ReportUsageMonthMapper {

    private ReportUsageMonthMapper() {}

    public static ReportUsageMonthResponse toReportUsageMonthResponse(
            LocalDateTime now,
            List<YearMonth> months,
            List<FamilySubList> familySubList,
            Map<Long, Map<YearMonth, Double>> subMonthlyMap,
            Long selfSubId,
            Long targetSubId
    ) {

        Map<Long, String> subIdToName =
                familySubList.stream()
                        .collect(Collectors.toMap(
                                FamilySubList::subId,
                                FamilySubList::subName
                        ));

        List<ReportUsageMonthResponse.SubUsageMonthResponse> responses =
                new ArrayList<>();

        // 가족 전체
        Map<YearMonth, Double> familyTotal = new LinkedHashMap<>();

        for (YearMonth m : months) {
            double sum = 0D;

            for (Map<YearMonth, Double> perSub : subMonthlyMap.values()) {
                sum += perSub.getOrDefault(m, 0D);
            }

            familyTotal.put(m, sum);
        }

        responses.add(
                build(-1L, "가족 전체", months, familyTotal)
        );

        responses.add(
                build(
                        selfSubId,
                        subIdToName.get(selfSubId),
                        months,
                        subMonthlyMap.getOrDefault(selfSubId, Map.of())
                )
        );

        if (targetSubId != null &&
                !targetSubId.equals(selfSubId)) {

            responses.add(
                    build(
                            targetSubId,
                            subIdToName.get(targetSubId),
                            months,
                            subMonthlyMap.getOrDefault(targetSubId, Map.of())
                    )
            );
        }

        return new ReportUsageMonthResponse(now, responses);
    }

    private static ReportUsageMonthResponse.SubUsageMonthResponse build(
            Long subId,
            String name,
            List<YearMonth> months,
            Map<YearMonth, Double> monthlyMap
    ) {

        List<ReportUsageMonthResponse.SubUsageMonthResponse
                .DataUsageMonthResponse> list =
                months.stream()
                        .map(m ->
                                new ReportUsageMonthResponse
                                        .SubUsageMonthResponse
                                        .DataUsageMonthResponse(
                                        m,
                                        monthlyMap.getOrDefault(m, 0D)
                                )
                        )
                        .toList();

        return new ReportUsageMonthResponse
                .SubUsageMonthResponse(
                subId,
                name,
                list
        );
    }
}
