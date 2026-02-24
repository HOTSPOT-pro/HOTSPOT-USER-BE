package hotspot.user.presentData.service.port;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.presentData.domain.SubUsage;

import java.util.List;
import java.util.Map;

public interface PresentDataRepository {

    Map<Long, String> findGiftGiverNames(List<Long> giftIds);

    Map<Long, SubUsage> findSubUsage(
            Map<Long, DataPeriod> subPeriodMap
    );
}
