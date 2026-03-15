package hotspot.user.familyReport.controller.port;

import hotspot.user.member.domain.FamilyRole;

public interface CancelFamilyReportSubscriptionService {

    void cancelSubscription(Long familyId, FamilyRole requesterRole);
}
