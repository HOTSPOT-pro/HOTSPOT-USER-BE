package hotspot.user.familyReport.controller.port;

import hotspot.user.familyReport.controller.request.UpdateFamilyReportReceiveDayRequest;
import hotspot.user.member.domain.FamilyRole;

public interface UpdateFamilyReportReceiveDayService {

    void updateReceiveDay(
            Long familyId,
            FamilyRole requesterRole,
            UpdateFamilyReportReceiveDayRequest request
    );
}
