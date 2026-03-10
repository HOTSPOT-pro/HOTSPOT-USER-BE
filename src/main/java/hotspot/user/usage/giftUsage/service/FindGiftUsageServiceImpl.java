package hotspot.user.usage.giftUsage.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.usage.giftUsage.controller.port.FindGiftUsageService;
import hotspot.user.usage.giftUsage.controller.response.GiftUsageListResponse;
import hotspot.user.usage.giftUsage.domain.GiftUsage;
import hotspot.user.usage.giftUsage.domain.mapper.GiftUsageResponseMapper;
import hotspot.user.usage.giftUsage.service.port.GiftUsageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindGiftUsageServiceImpl implements FindGiftUsageService {

    private final GiftUsageRepository giftUsageRepository;
    private final PresentDataRepository presentDataRepository;
    private final SubscriptionService subscriptionService;
    private final Clock clock;

    @Override
    public GiftUsageListResponse findGiftUsages(Long memberId) {

        Subscription subscription = subscriptionService.findByMemberId(memberId);

        List<GiftUsage> usage =
                giftUsageRepository
                        .findGiftUsageList(subscription.getId());

        List<Long> giftIds =
                usage
                        .stream()
                        .map(GiftUsage::giftId)
                        .toList();

        Map<Long, String> giftIdToUserName =
                presentDataRepository.findGiftGiverNames(giftIds);

        LocalDateTime now = LocalDateTime.now(clock);

        return GiftUsageResponseMapper
                .toGiftUsageListResponse(now, usage, giftIdToUserName);
    }
}
