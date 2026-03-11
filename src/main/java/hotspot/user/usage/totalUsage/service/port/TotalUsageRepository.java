package hotspot.user.usage.totalUsage.service.port;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.usage.totalUsage.repository.schema.TotalUsage;

public interface TotalUsageRepository {

    TotalUsage findTotalUsage(
            Long subId,
            Long familyId,
            DataPeriod dataPeriod
    );
}
