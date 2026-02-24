package hotspot.user.presentData.service;

import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.presentData.controller.port.FindFamilyDataService;
import hotspot.user.presentData.controller.response.FamilyDataResponse;
import hotspot.user.presentData.domain.SubUsage;
import hotspot.user.presentData.domain.mapper.FamilyDataMapper;
import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import hotspot.user.usage.familyUsage.domain.mapper.FamilyUsageMapper;
import hotspot.user.usage.familyUsage.service.schema.FamilySubList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FindFamilyDataServiceImpl implements FindFamilyDataService {

    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final PresentDataRepository presentDataRepository;
    private final FamilyDataMapper familyDataMapper;

    @Transactional(readOnly = true)
    @Override
    public FamilyDataResponse findFamilyData(Long memberId, Long familyId) {

        Subscription self =
                subscriptionService.findByMemberId(memberId);

        List<FamilySubList> familySubList =
                familySubscriptionRepository.findByFamilyId(familyId)
                        .stream()
                        .map(FamilyUsageMapper::toFamilySubList)
                        .toList();

        Map<Long, DataPeriod> subPeriodMap =
                buildSubPeriodMap(self, familySubList);

        Map<Long, SubUsage> usageMap =
                presentDataRepository.findSubUsage(subPeriodMap);

        SubUsage selfUsage =
                usageMap.getOrDefault(
                        self.getId(),
                        new SubUsage(0, 0)
                );

        return familyDataMapper.toFamilyDataResponse(
                self.getId(),
                selfUsage,
                familySubList,
                usageMap
        );
    }

    /**
     * subPeriodMap 생성
     */
    private Map<Long, DataPeriod> buildSubPeriodMap(
            Subscription self,
            List<FamilySubList> familySubList
    ) {

        List<Long> subIds =
                familySubList.stream()
                        .map(FamilySubList::subId)
                        .toList();

        Map<Long, DataPeriod> periodMap =
                subscriptionRepository.findDataPeriodsBySubIds(subIds);

        Map<Long, DataPeriod> result =
                new LinkedHashMap<>();

        result.put(
                self.getId(),
                self.getPlan().getDataPeriod()
        );

        result.putAll(periodMap);

        return result;
    }
}
