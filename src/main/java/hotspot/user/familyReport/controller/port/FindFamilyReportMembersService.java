package hotspot.user.familyReport.controller.port;

import hotspot.user.familyReport.controller.response.FamilyReportMembersResponse;

public interface FindFamilyReportMembersService {

    FamilyReportMembersResponse findMembers(Long familyId);
}
