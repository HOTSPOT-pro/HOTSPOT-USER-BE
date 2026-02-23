package hotspot.user.presentData.service.port;

import java.util.List;
import java.util.Map;

public interface PresentDataRepository {

    Map<Long, String> findGiftGiverNames(List<Long> giftIds);
}
