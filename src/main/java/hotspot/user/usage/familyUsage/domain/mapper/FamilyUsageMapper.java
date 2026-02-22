package hotspot.user.usage.familyUsage.domain.mapper;

import java.util.List;

import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.usage.familyUsage.controller.response.FamilyUsageResponse;
import hotspot.user.usage.familyUsage.domain.FamilyUsage;
import hotspot.user.usage.familyUsage.domain.FamilySubUsage;
import hotspot.user.usage.familyUsage.service.schema.FamilySubList;

public class FamilyUsageMapper {

    public static FamilySubList toFamilySubList(FamilySubscription fs) {
        return new FamilySubList(
                fs.getSubscription().getId(),
                fs.getSubscription().getMember().getName()
        );
    }

    public static FamilyUsageResponse toResponse(
            FamilyUsage usage,
            List<FamilySubList> subs
    ) {

        List<FamilyUsageResponse.FamilySubUsageResponse> subResponses =
                subs.stream()
                        .map(sub -> toSubResponse(usage, sub))
                        .toList();

        return new FamilyUsageResponse(
                usage.familyLimitGb(),
                usage.familyUsedGb(),
                usage.familyRemainGb(),
                usage.familyUsagePercent(),
                subResponses
        );
    }

    private static FamilyUsageResponse.FamilySubUsageResponse toSubResponse(
            FamilyUsage usage,
            FamilySubList sub
    ) {

        FamilySubUsage familySubUsage = usage.getSubOrZero(sub.subId());

        return new FamilyUsageResponse.FamilySubUsageResponse(
                sub.subId(),
                sub.subName(),
                familySubUsage.limitGb(),
                familySubUsage.familyUsedGb(),
                familySubUsage.remainGb(),
                familySubUsage.usagePercent()
        );
    }
}
