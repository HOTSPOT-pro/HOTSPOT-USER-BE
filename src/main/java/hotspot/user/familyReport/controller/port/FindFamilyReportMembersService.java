package hotspot.user.familyReport.controller.port;

import java.util.List;

import hotspot.user.familyReport.controller.response.FamilyReportMemberResponse;

public interface FindFamilyReportMembersService {

    List<FamilyReportMemberResponse> findMembers(Long familyId);
}
