package hotspot.user.usage.totalUsage.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.usage.totalUsage.controller.port.FindTotalUsageService;
import hotspot.user.usage.totalUsage.controller.response.TotalUsageResponse;
import hotspot.user.usage.totalUsage.domain.mapper.TotalUsageResponseMapper;
import hotspot.user.usage.totalUsage.repository.schema.TotalUsage;
import hotspot.user.usage.totalUsage.service.port.TotalUsageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindTotalUsageServiceImpl implements FindTotalUsageService {

    private final SubscriptionService subscriptionService;
    private final TotalUsageRepository totalUsageRepository;
    private final Clock clock;

    @Override
    public TotalUsageResponse findTotalUsage(Long memberId, Long familyId) {

        Subscription subscription = subscriptionService.findByMemberId(memberId);


        DataPeriod dataPeriod =
                subscription.getPlan().getDataPeriod();

        TotalUsage usage =
                totalUsageRepository.findTotalUsage(
                        subscription.getId(),
                        familyId,
                        dataPeriod);

        LocalDateTime now = LocalDateTime.now(clock);

        return TotalUsageResponseMapper.toTotalUsageResponse(subscription, usage, now);
    }
}
