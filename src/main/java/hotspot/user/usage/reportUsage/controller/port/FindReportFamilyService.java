package hotspot.user.usage.reportUsage.controller.port;

import java.util.List;

import hotspot.user.usage.reportUsage.controller.response.ReportFamilyResponse;

public interface FindReportFamilyService {

    List<ReportFamilyResponse> findReportFamily(Long familyId);
}
