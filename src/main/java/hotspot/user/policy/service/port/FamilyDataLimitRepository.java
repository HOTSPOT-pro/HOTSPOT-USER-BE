package hotspot.user.policy.service.port;

import hotspot.user.policy.infrastructure.schema.FamilyDataControl;

public interface FamilyDataLimitRepository {

    FamilyDataControl findFamilyDataLimit(Long familyId);
}
