package hotspot.user.presentData.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import hotspot.user.presentData.service.port.PresentDataRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PresentDataRepositoryImpl implements PresentDataRepository {

    private final PresentDataJpaRepository presentDataJpaRepository;

    @Override
    public Map<Long, String> findGiftGiverNames(List<Long> giftIds) {

        if (giftIds == null || giftIds.isEmpty()) {
            return Map.of();
        }

        return presentDataJpaRepository
                .findGiftGivers(giftIds)
                .stream()
                .collect(Collectors.toMap(
                        PresentDataJpaRepository.GiftGiverRow::getGiftId,
                        PresentDataJpaRepository.GiftGiverRow::getGiverName
                ));
    }
}
