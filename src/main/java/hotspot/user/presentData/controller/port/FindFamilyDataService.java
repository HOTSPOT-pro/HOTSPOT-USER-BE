package hotspot.user.presentData.controller.port;

import hotspot.user.presentData.controller.response.FamilyDataResponse;

public interface FindFamilyDataService {

    FamilyDataResponse findFamilyData(Long memberId, Long familyId);
}
