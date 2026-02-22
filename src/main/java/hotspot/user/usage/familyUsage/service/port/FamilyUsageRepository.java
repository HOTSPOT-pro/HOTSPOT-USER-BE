package hotspot.user.usage.familyUsage.service.port;

import java.util.List;

import hotspot.user.usage.familyUsage.domain.FamilyUsage;
import hotspot.user.usage.familyUsage.service.schema.FamilySubList;

public interface FamilyUsageRepository {

    FamilyUsage findFamilyUsage(Long familyId, List<FamilySubList> familySubList);
}
