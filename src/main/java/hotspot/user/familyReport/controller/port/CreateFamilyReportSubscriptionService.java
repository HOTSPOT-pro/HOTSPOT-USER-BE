package hotspot.user.familyReport.controller.port;

import hotspot.user.familyReport.controller.request.CreateFamilyReportSubscriptionRequest;
import hotspot.user.member.domain.FamilyRole;

public interface CreateFamilyReportSubscriptionService {

    void createSubscription(
            Long familyId,
            FamilyRole requesterRole,
            CreateFamilyReportSubscriptionRequest request
    );
}
