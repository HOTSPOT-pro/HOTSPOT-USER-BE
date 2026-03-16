package hotspot.user.familyReport.controller.response;

import java.time.DayOfWeek;
import java.util.List;

import lombok.Builder;

@Builder
public record FamilyReportMembersResponse(
        DayOfWeek receiveDay,
        List<FamilyReportMemberResponse> members
) {
}
