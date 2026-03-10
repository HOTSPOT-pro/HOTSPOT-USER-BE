package hotspot.user.usage.giftUsage.controller.port;

import hotspot.user.usage.giftUsage.controller.response.GiftUsageListResponse;

public interface FindGiftUsageService {

    GiftUsageListResponse findGiftUsages(Long memberId);

}
