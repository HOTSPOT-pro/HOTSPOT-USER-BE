package hotspot.user.usage.familyUsage.service.port;

import java.util.List;

import hotspot.user.usage.familyUsage.controller.response.FamilyUsageResponse;
import hotspot.user.usage.familyUsage.service.schema.FamilySubList;

public interface FamilyUsageRepository {

    FamilyUsageResponse findFamilyUsage(Long familyId, List<FamilySubList> familySubList);
}
