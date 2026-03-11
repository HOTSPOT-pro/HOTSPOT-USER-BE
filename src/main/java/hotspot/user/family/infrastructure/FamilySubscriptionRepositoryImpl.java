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

    // 회선 id 목록 받아서 해당하는 FamilySubscription 리스트 리턴
    @Override
    public List<FamilySubscription> findAllBySubIdIn(List<Long> subIds) {
        return jpaRepository.findAllBySubscriptionSubIdIn(subIds).stream()
                .map(FamilySubscriptionEntity::entityToDomain)
                .toList();
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
    public Optional<Long> findFamilyIdByMemberId(Long memberId) {
        return jpaRepository.findFamilyIdByMemberId(memberId);
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

    @Override
    public void updateDataLimit(Long subId, long dataLimit) {
        jpaRepository.updateDataLimit(subId, dataLimit);
    }

    // 구성원별 데이터 한도 조회
    @Override
    public FamilySubDataLimit findDataLimitBySubId(Long subId) {
        return jpaRepository.findDataLimitBySubId(subId)
                .map(row -> FamilySubDataLimit.builder()
                        .familyId(row.getFamilyId())
                        .name(row.getName())
                        .isLocked(row.getIsLocked())
                        .dataLimit(row.getDataLimit())
                        .familyDataAmount(row.getFamilyDataAmount())
                        .build())
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));
    }
}
