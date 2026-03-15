package hotspot.user.familyReport.controller.response;

import hotspot.user.member.domain.FamilyRole;
import lombok.Builder;

@Builder
public record FamilyReportMemberResponse(
        Long subId,
        String name,
        FamilyRole familyRole,
        Long reportId
) {
}
