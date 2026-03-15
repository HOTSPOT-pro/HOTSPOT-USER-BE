package hotspot.user.familyReport.controller.port;

import hotspot.user.familyReport.controller.response.FamilyReportSubscriptionResponse;

public interface FindFamilyReportSubscriptionService {

    FamilyReportSubscriptionResponse findSubscription(Long familyId);
}
