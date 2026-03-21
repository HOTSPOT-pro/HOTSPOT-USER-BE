package hotspot.user.presentData.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.presentData.domain.PresentData;
import hotspot.user.presentData.domain.SubUsage;
import hotspot.user.presentData.infrastructure.entity.PresentDataEntity;
import hotspot.user.presentData.service.port.PresentDataRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PresentDataRepositoryImpl implements PresentDataRepository {

    private final PresentDataJpaRepository presentDataJpaRepository;
    private final FamilySubUsageRedisRepository redisRepository;

    @Override
    public List<PresentData> findPresentReceive(Long targetSubId) {
        return presentDataJpaRepository.findAllByTargetSubId(targetSubId)
                .stream()
                .map(PresentDataEntity::entityToDomain)
                .toList();
    }

    @Override
    public List<PresentData> findPresentProvide(Long provideSubId) {
        return presentDataJpaRepository.findAllByProviderSubId(provideSubId)
                .stream()
                .map(PresentDataEntity::entityToDomain)
                .toList();
    }

    @Override
    public Map<Long, String> findGiftGiverNames(List<Long> giftIds) {
        if (giftIds == null || giftIds.isEmpty()) {
            return Map.of();
        }

        return presentDataJpaRepository.findGiftGivers(giftIds)
                .stream()
                .collect(Collectors.toMap(
                        PresentDataJpaRepository.GiftGiverRow::getGiftId,
                        PresentDataJpaRepository.GiftGiverRow::getGiverName
                ));
    }

    @Override
    public Map<Long, SubUsage> findSubUsage(Map<Long, DataPeriod> subPeriodMap) {
        return redisRepository.findUsageAndLimit(subPeriodMap);
    }

    @Override
    public PresentData sendPresentData(PresentData presentData) {
        PresentDataEntity entity = PresentDataEntity.domainToEntity(presentData);
        return presentDataJpaRepository.save(entity).entityToDomain();
    }

    @Override
    public long sumMonthlySentKb(Long providerSubId, LocalDateTime start, LocalDateTime end) {
        return presentDataJpaRepository.sumMonthlySentKb(providerSubId, start, end);
    }
}
