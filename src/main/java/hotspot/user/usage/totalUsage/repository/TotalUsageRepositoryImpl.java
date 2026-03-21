package hotspot.user.usage.totalUsage.repository;

import org.springframework.stereotype.Repository;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.usage.totalUsage.repository.schema.TotalUsage;
import hotspot.user.usage.totalUsage.service.port.TotalUsageRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TotalUsageRepositoryImpl implements TotalUsageRepository {

    private final TotalUsageRedisRepository redisRepository;

    @Override
    public TotalUsage findTotalUsage(
            Long subId,
            Long familyId,
            DataPeriod dataPeriod
    ) {
        return redisRepository.findTotalUsage(subId, familyId, dataPeriod);
    }
}
