package hotspot.user.usage.familyUsage.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.usage.familyUsage.controller.port.FindFamilyUsageService;
import hotspot.user.usage.familyUsage.controller.response.FamilyUsageResponse;
import hotspot.user.usage.familyUsage.domain.mapper.FamilyUsageMapper;
import hotspot.user.usage.familyUsage.service.port.FamilyUsageRepository;
import hotspot.user.usage.familyUsage.service.schema.FamilySubList;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFamilyUsageServiceImpl implements FindFamilyUsageService {
    private final FamilyUsageRepository findFamilyUsageRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;


    @Transactional(readOnly = true)
    @Override
    public FamilyUsageResponse findFamilyUsage(Long familyId) {

        List<FamilySubList> familySubList = familySubscriptionRepository.findByFamilyId(familyId)
                .stream()
                .map(FamilyUsageMapper::toFamilySubList)
                .toList();

        return findFamilyUsageRepository.findFamilyUsage(familyId, familySubList);
    }
}
