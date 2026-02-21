package hotspot.user.policy.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.policy.domain.BlockedServiceSub;
import hotspot.user.policy.infrastructure.entity.AppBlockedServiceEntity;
import hotspot.user.policy.infrastructure.entity.BlockedServiceSubEntity;
import hotspot.user.policy.service.port.BlockedServiceSubRepository;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BlockedServiceSubRepositoryImpl implements BlockedServiceSubRepository {
    private final BlockedServiceSubJpaRepository jpaRepository;

    @Override
    public List<BlockedServiceSub> findBySubId(Long subId) {
        return jpaRepository.findBySubscriptionSubId(subId).stream()
                .map(BlockedServiceSubEntity::entityToDomain)
                .toList();
    }

    @Override
    public List<Long> findActiveServiceIdsBySubId(Long subId) {
        return jpaRepository.findActiveServiceIdsBySubId(subId);
    }

    @Override
    @Transactional
    public void saveAll(Long subId, Set<Long> serviceIds) {
        if (serviceIds.isEmpty()) {
            return;
        }

        // 1. 복구할 수 있는 기존 데이터(삭제상태 포함)를 한 번에 조회
        List<BlockedServiceSubEntity> existingEntities =
                jpaRepository.findBySubIdAndServiceIdsIncludeDeleted(subId, serviceIds);

        Map<Long, BlockedServiceSubEntity> existingMap = existingEntities.stream()
                .collect(Collectors.toMap(
                        e -> e.getAppBlockedService().getAppBlockedServiceId(),
                        e -> e
                ));

        List<BlockedServiceSubEntity> toSave = new ArrayList<>();

        // 2. 서비스에서 넘겨준 '추가 대상' ID들을 돌며 복구 또는 신규 생성
        for (Long serviceId : serviceIds) {
            if (existingMap.containsKey(serviceId)) {
                // 이미 이력이 있으면 -> isDeleted만 false로 바꿔서 저장 (Update)
                BlockedServiceSubEntity existing = existingMap.get(serviceId);
                toSave.add(BlockedServiceSubEntity.builder()
                        .blockedServiceSubId(existing.getBlockedServiceSubId())
                        .subscription(existing.getSubscription())
                        .appBlockedService(existing.getAppBlockedService())
                        .isDeleted(false)
                        .build());
            } else {
                // 이력이 아예 없으면 -> 신규 생성 (Insert)
                toSave.add(BlockedServiceSubEntity.builder()
                        .subscription(SubscriptionEntity.builder().subId(subId).build())
                        .appBlockedService(AppBlockedServiceEntity.builder()
                                .appBlockedServiceId(serviceId).build())
                        .isDeleted(false)
                        .build());
            }
        }

        jpaRepository.saveAll(toSave);
    }

    // // 서비스에서 넘겨준 삭제 대상 ID들을 한 번에 비활성화 (Bulk Update)
    @Override
    public void deleteAll(Long subId, Set<Long> serviceIds) {
        if (!serviceIds.isEmpty()) {

            jpaRepository.bulkSoftDelete(subId, serviceIds);
        }
    }
}
