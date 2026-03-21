package hotspot.user.presentData.domain.mapper;

import java.util.List;

import hotspot.user.common.util.UsageCalculator;
import hotspot.user.presentData.controller.response.PresentDataResponse;
import hotspot.user.presentData.domain.PresentData;

public class PresentDataMapper {

    public static PresentDataResponse toPresentDataReceiveResponse(List<PresentData> list) {

        long totalKb = list.stream()
                .mapToLong(PresentData::getDataAmount)
                .sum();

        double totalGb = UsageCalculator.kbToGb(totalKb);

        List<PresentDataResponse.PresentItemResponse> items =
                list.stream()
                        .map(p -> new PresentDataResponse.PresentItemResponse(
                                p.getProvideSubscription().getId(),
                                p.getProvideSubscription().getMember().getName(),
                                UsageCalculator.kbToGb(p.getDataAmount()),
                                p.getCreatedTime()
                        ))
                        .toList();

        return new PresentDataResponse(totalGb, items);
    }

    public static PresentDataResponse toPresentDataProvideResponse(List<PresentData> list) {
        long totalKb = list.stream()
                .mapToLong(PresentData::getDataAmount)
                .sum();

        double totalGb = UsageCalculator.kbToGb(totalKb);

        List<PresentDataResponse.PresentItemResponse> items =
                list.stream()
                        .map(p -> new PresentDataResponse.PresentItemResponse(
                                p.getTargetSubscription().getId(),
                                p.getTargetSubscription().getMember().getName(),
                                UsageCalculator.kbToGb(p.getDataAmount()),
                                p.getCreatedTime()
                        ))
                        .toList();

        return new PresentDataResponse(totalGb, items);
    }
}
