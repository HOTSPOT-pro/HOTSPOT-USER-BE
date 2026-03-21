package hotspot.user.usage.subscriptionUsage.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.usage.subscriptionUsage.controller.port.FindSubscriptionUsageService;
import hotspot.user.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;
import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;
import hotspot.user.usage.subscriptionUsage.domain.mapper.SubscriptionUsageResponseMapper;
import hotspot.user.usage.subscriptionUsage.service.port.SubscriptionUsageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindSubscriptionUsageServiceImpl implements FindSubscriptionUsageService {

    private final SubscriptionUsageRepository subscriptionUsageRepository;
    private final SubscriptionService subscriptionService;
    private final Clock clock;

    @Override
    public SubscriptionUsageResponse findSubscriptionUsage(Long memberId) {

        Subscription subscription = subscriptionService.findByMemberId(memberId);

        DataPeriod dataPeriod = subscription.getPlan().getDataPeriod();

        SubscriptionUsage usage =
                subscriptionUsageRepository
                        .findSubscriptionUsage(subscription.getId(), dataPeriod);

        LocalDateTime now = LocalDateTime.now(clock);

        return SubscriptionUsageResponseMapper.toResponse(
                usage,
                subscription.getPlan().getName(),
                now
        );
    }
}
