package hotspot.user.presentData.service.port;

import java.util.List;
import java.util.Map;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.presentData.domain.SubUsage;

public interface PresentDataRepository {

    Map<Long, String> findGiftGiverNames(List<Long> giftIds);

    Map<Long, SubUsage> findSubUsage(
            Map<Long, DataPeriod> subPeriodMap
    );
}
