package hotspot.user.usage.totalUsage.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
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
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final TotalUsageRepository totalUsageRepository;
    private final Clock clock;

    @Override
    public TotalUsageResponse findTotalUsage(Long memberId) {

        Subscription subscription = subscriptionService.findByMemberId(memberId);

        Long familyId = familySubscriptionRepository.findFamilyIdByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

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
