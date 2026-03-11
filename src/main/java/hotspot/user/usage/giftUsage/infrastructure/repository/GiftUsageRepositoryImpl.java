package hotspot.user.usage.giftUsage.infrastructure.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import hotspot.user.usage.giftUsage.domain.GiftUsage;
import hotspot.user.usage.giftUsage.service.port.GiftUsageRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GiftUsageRepositoryImpl implements GiftUsageRepository {

    private final GiftUsageRedisRepository redisRepository;

    @Override
    public List<GiftUsage> findGiftUsageList(Long subId) {
        return redisRepository.findGiftUsageList(subId);
    }
}
