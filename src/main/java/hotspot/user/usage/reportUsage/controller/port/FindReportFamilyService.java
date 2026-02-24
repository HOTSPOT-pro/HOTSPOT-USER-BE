package hotspot.user.usage.reportUsage.controller.port;

import hotspot.user.usage.reportUsage.controller.response.ReportFamilyResponse;

import java.util.List;

public interface FindReportFamilyService {

    List<ReportFamilyResponse> findReportFamily(Long familyId);
}
