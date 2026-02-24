package hotspot.user.usage.familyUsage.service;

import java.sql.Time;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.usage.familyUsage.controller.port.FindFamilyUsageService;
import hotspot.user.usage.familyUsage.controller.response.FamilyUsageResponse;
import hotspot.user.usage.familyUsage.domain.FamilyUsage;
import hotspot.user.usage.familyUsage.domain.mapper.FamilyUsageMapper;
import hotspot.user.usage.familyUsage.service.port.FamilyUsageRepository;
import hotspot.user.usage.familyUsage.service.schema.FamilySubList;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFamilyUsageServiceImpl implements FindFamilyUsageService {

    private final FamilyUsageRepository findFamilyUsageRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    @Override
    public FamilyUsageResponse findFamilyUsage(Long familyId) {

        List<FamilySubList> familySubList = familySubscriptionRepository.findByFamilyId(familyId)
                .stream()
                .map(FamilyUsageMapper::toFamilySubList)
                .toList();

        FamilyUsage familyUsage = findFamilyUsageRepository.findFamilyUsage(familyId, familySubList);

        LocalDateTime now = LocalDateTime.now(clock);

        return FamilyUsageMapper.toFamilyUsageResponse(familyUsage, familySubList, now);
    }
}
