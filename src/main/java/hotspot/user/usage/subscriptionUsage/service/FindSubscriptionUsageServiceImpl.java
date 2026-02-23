package hotspot.user.usage.subscriptionUsage.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.usage.subscriptionUsage.controller.port.FindSubscriptionUsageService;
import hotspot.user.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;
import hotspot.user.usage.subscriptionUsage.domain.GiftUsage;
import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;
import hotspot.user.usage.subscriptionUsage.domain.mapper.SubscriptionUsageMapper;
import hotspot.user.usage.subscriptionUsage.service.port.SubscriptionUsageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindSubscriptionUsageServiceImpl implements FindSubscriptionUsageService {

    private final SubscriptionUsageRepository subscriptionUsageRepository;
    private final PresentDataRepository presentDataRepository;
    private final SubscriptionService subscriptionService;

    @Override
    public SubscriptionUsageResponse findSubscriptionUsage(Long memberId) {

        Subscription subscription = subscriptionService.findByMemberId(memberId);

        // Redis에서 숫자 + giftId 조회
        SubscriptionUsage usage =
                subscriptionUsageRepository
                        .findSubscriptionUsage(subscription.getId());

        // giftId 리스트 추출
        List<Long> giftIds =
                usage.gifts()
                        .stream()
                        .map(GiftUsage::giftId)
                        .toList();

        // giftId → username 매핑
        Map<Long, String> giftIdToUserName =
                presentDataRepository.findGiftGiverNames(giftIds);

        // Mapper에 전달
        return SubscriptionUsageMapper
                .toSubscriptionUsageResponse(
                        usage,
                        giftIdToUserName
                );
    }
}
