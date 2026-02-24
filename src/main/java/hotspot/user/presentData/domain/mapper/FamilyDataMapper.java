package hotspot.user.presentData.domain.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import hotspot.user.presentData.controller.response.FamilyDataResponse;
import hotspot.user.presentData.domain.SubUsage;
import hotspot.user.usage.familyUsage.service.schema.FamilySubList;

@Component
public class FamilyDataMapper {

    public FamilyDataResponse toFamilyDataResponse(
            Long selfSubId,
            SubUsage selfUsage,
            List<FamilySubList> familySubList,
            Map<Long, SubUsage> usageMap
    ) {

        List<FamilyDataResponse.subUsageResponse> subResponses =
                familySubList.stream()
                        .filter(sub -> !sub.subId().equals(selfSubId))
                        .map(sub -> {

                            SubUsage usage =
                                    usageMap.getOrDefault(
                                            sub.subId(),
                                            new SubUsage(0, 0)
                                    );

                            return new FamilyDataResponse.subUsageResponse(
                                    sub.subId(),
                                    sub.subName(),
                                    usage.limitGb(),
                                    usage.usedGb(),
                                    usage.percent()
                            );
                        })
                        .toList();

        return new FamilyDataResponse(
                selfSubId,
                selfUsage.remainGb(),
                subResponses
        );
    }
}
