package hotspot.user.usage.familyUsage.controller.port;

import hotspot.user.usage.familyUsage.controller.response.FamilyUsageResponse;

public interface FindFamilyUsageService {

    FamilyUsageResponse findFamilyUsage(Long familyId);
}
