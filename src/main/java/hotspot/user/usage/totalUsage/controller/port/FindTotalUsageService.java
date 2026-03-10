package hotspot.user.usage.totalUsage.controller.port;

import hotspot.user.usage.totalUsage.controller.response.TotalUsageResponse;

public interface FindTotalUsageService {

    TotalUsageResponse findTotalUsage(Long memberId, Long familyId);
}
