package hotspot.user.usage.familyUsage.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import hotspot.user.usage.familyUsage.controller.response.FamilyUsageResponse;
import hotspot.user.usage.familyUsage.domain.FamilyUsage;
import hotspot.user.usage.familyUsage.domain.mapper.FamilyUsageMapper;
import hotspot.user.usage.familyUsage.service.port.FamilyUsageRepository;
import hotspot.user.usage.familyUsage.service.schema.FamilySubList;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyUsageRepositoryImpl implements FamilyUsageRepository {

    private final FamilyUsageRedisRepository redisRepository;

    @Override
    public FamilyUsage findFamilyUsage(
            Long familyId,
            List<FamilySubList> familySubList
    ) {

        List<Long> subIds = familySubList.stream()
                .map(FamilySubList::subId)
                .toList();

        return redisRepository.findFamilyAndSubData(familyId, subIds);
    }
}
