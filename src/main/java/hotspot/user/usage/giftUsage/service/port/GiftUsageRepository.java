package hotspot.user.usage.giftUsage.service.port;

import java.util.List;

import hotspot.user.usage.giftUsage.domain.GiftUsage;

public interface GiftUsageRepository {

    List<GiftUsage> findGiftUsageList(Long subId);
}
