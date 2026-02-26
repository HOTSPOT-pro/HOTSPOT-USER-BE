package hotspot.user.family.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.domain.FamilySubDataLimit;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.infrastructure.entity.FamilySubscriptionEntity;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilySubscriptionRepositoryImpl implements FamilySubscriptionRepository {

    private final FamilySubscriptionJpaRepository jpaRepository;

    @Override
    public Optional<FamilySubscription> findBySubId(Long subId) {
        return jpaRepository.findBySubscriptionSubId(subId)
                .map(FamilySubscriptionEntity::entityToDomain);
    }

    @Override
    public List<FamilySubscription> findByFamilyId(Long familyId) {
        return jpaRepository.findByFamilyFamilyId(familyId).stream()
                .map(FamilySubscriptionEntity::entityToDomain)
                .toList();
    }

    @Override
    public Optional<FamilySubscription> findByMemberId(Long memberId) {
        return jpaRepository.findBySubscriptionMemberId(memberId)
                .map(FamilySubscriptionEntity::entityToDomain);
    }

    @Override
    public FamilySubscription save(FamilySubscription familySubscription) {
        FamilySubscriptionEntity entity = FamilySubscriptionEntity.domainToEntity(familySubscription);
        FamilySubscriptionEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.entityToDomain();
    }

    @Override
    public void updatePriorities(List<FamilySubscription> subscriptions) {
        for (FamilySubscription sub : subscriptions) {
            jpaRepository.updatePriority(sub.getSubscription().getId(), sub.getPriority());
        }
    }

    // 구성원별 데이터 한도 조회
    @Override
    public FamilySubDataLimit findDataLimitBySubId(Long subId) {
        return jpaRepository.findDataLimitBySubId(subId)
                .map(row -> FamilySubDataLimit.builder()
                        .name(row.getName())
                        .isLocked(row.getIsLocked())
                        .dataLimit(row.getDataLimit())
                        .familyDataAmount(row.getFamilyDataAmount())
                        .build())
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));
    }
}
