package hotspot.user.usage.reportUsage.domain.mapper;

import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.usage.reportUsage.controller.response.ReportFamilyResponse;

public class ReportFamilyMapper {

    public static ReportFamilyResponse toReportFamilyResponse(FamilySubscription familySubscription) {
        return new ReportFamilyResponse(
                familySubscription.getSubscription().getId(),
                familySubscription.getSubscription().getMember().getName()
        );
    }
}
